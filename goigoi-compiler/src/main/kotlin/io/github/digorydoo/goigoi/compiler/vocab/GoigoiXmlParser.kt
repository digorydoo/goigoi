package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.*
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.ParsingFailed
import io.github.digorydoo.goigoi.compiler.check.check
import io.github.digorydoo.goigoi.compiler.check.checkPhrase
import io.github.digorydoo.goigoi.compiler.check.checkSentence
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint
import oracle.xml.parser.v2.DOMParser
import oracle.xml.parser.v2.MyDOMParser
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream

class GoigoiXmlParser {
    private lateinit var vocab: GoigoiVocab
    private var topic: GoigoiTopic? = null
    private val subheader = IntlString()

    fun parse(stream: InputStream, voc: GoigoiVocab, defaultTopic: GoigoiTopic? = null) {
        val parser = MyDOMParser()
        parser.setErrorStream(System.err)
        parser.setValidationMode(DOMParser.NONVALIDATING)
        parser.showWarnings(true)
        parser.parse(stream)

        vocab = voc
        topic = defaultTopic ?: voc.topics.lastOrNull() // unyts are appended to this topic until a new topic is seen
        subheader.clear()

        val root = parser.document.documentElement

        if (root.nodeName != "vocabulary") {
            throw ParsingFailed("XML root is not vocabulary", root)
        }

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "kanji" -> readKanji(tag)
                "topic" -> readTopic(tag)
                "subheader" -> readSubheader(tag)
                "unit" -> readUnyt(tag)
                else -> throw ParsingFailed("Tag not handled", tag)
            }
        }

        // Note that voc.check() cannot be called from here, because this function just reads one XML.
        // It will be called from compileGoigoi instead.
    }

    private fun readKanji(root: Element) {
        checkAttributes(root, arrayOf()) { ParsingFailed("readKanji: $it", root) }

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "set" -> readKanjiSetTag(tag)
                "freq" -> readKanjiFreq(tag)
                "dont_confuse" -> readKanjiDontConfuse(tag)
                else -> throw ParsingFailed("Tag not handled", tag)
            }
        }
    }

    private fun readKanjiSetTag(root: Element) {
        val lvl = getOptionalAttr(root, "lvl")?.let { JLPTLevel.fromStringNotNull(it) }

        val schoolYear = getOptionalAttr(root, "schoolyear")
            ?.let { it.toIntOrNull() ?: throw ParsingFailed("Value of school year not an int: $it", root) }
            ?.also { year -> require(year in 1 .. 6) { "Bad value for school year: $year" } }

        checkAttributes(root, arrayOf("lvl", "schoolyear", "rem")) { ParsingFailed("readKanjiSetTag: $it", root) }

        val kanjis = root.textContent
            ?.filter { !it.isWhitespace() }
            ?.takeIf { it.isNotEmpty() }
            ?: throw ParsingFailed("Empty value in kanji index tag!", root)

        val badChars = kanjis.filter { !it.isCJK() }

        if (badChars.isNotEmpty()) {
            throw ParsingFailed("Tag contains content that aren't kanji: $badChars", root)
        }

        val numNotNull = arrayOf<Any?>(lvl, schoolYear).filterNotNull().size

        val set = when {
            numNotNull > 1 -> throw ParsingFailed("Tag can have only one of attributes: lvl, schoolyear", root)
            lvl != null -> vocab.manualKanjiLevels[lvl]
                ?: mutableSetOf<Char>().also { vocab.manualKanjiLevels[lvl] = it }
            schoolYear != null -> vocab.kanjiBySchoolYear[schoolYear]
                ?: mutableSetOf<Char>().also { vocab.kanjiBySchoolYear[schoolYear] = it }
            else -> throw ParsingFailed("Tag requires one of attributes: lvl, schoolyear", root)
        }

        kanjis.forEach { kanji ->
            if (schoolYear != null && set.contains(kanji)) {
                throw ParsingFailed("Kanji $kanji already in same school year: $schoolYear", root)
            }
            set.add(kanji)
        }
    }

    private fun readKanjiFreq(root: Element) {
        checkAttributes(root, arrayOf()) { ParsingFailed("readKanjiFreq: $it", root) }

        val kanjis = root.textContent
            ?.filter { !it.isWhitespace() }
            ?.takeIf { it.isNotEmpty() }
            ?: throw ParsingFailed("Empty value in kanji freq tag!", root)

        val badChars = kanjis.filter { !it.isCJK() }

        if (badChars.isNotEmpty()) {
            throw ParsingFailed("Tag contains content that aren't kanji: $badChars", root)
        }

        if (vocab.kanjiByFreq.isNotEmpty()) {
            throw ParsingFailed("Kanji by frequency has already been defined!", root)
        }

        vocab.kanjiByFreq = kanjis
    }

    private fun readKanjiDontConfuse(root: Element) {
        checkAttributes(root, arrayOf()) { ParsingFailed("readKanjiDontConfuse: $it", root) }

        val kanjis = root.textContent?.filter { !it.isWhitespace() }

        if (kanjis == null || kanjis.length <= 1) {
            throw ParsingFailed("dont_confuse entry needs to mention at least two kanjis", root)
        } else if (kanjis.length >= 7) {
            throw ParsingFailed("dont_confuse entry should be split into two: $kanjis", root)
        }

        val badChars = kanjis.filter { !it.isCJK() && !it.isKatakana() && !it.isHiragana() }

        if (badChars.isNotEmpty()) {
            throw ParsingFailed("Tag contains content that aren't kanji: $badChars", root)
        }

        val discouragedKana = kanjis.filter {
            it == 'カ' || it == 'エ' || it == 'ロ' || it == 'タ' || it == 'ニ' || it == 'ハ'
        }

        if (discouragedKana.isNotEmpty()) {
            throw ParsingFailed(
                "Do not use these kana characters, because there are kanji that look the same: $discouragedKana",
                root
            )
        }

        val badPairs = arrayOf(Pair('末', '未'))

        badPairs.forEach { (a, b) ->
            if (kanjis.contains(a) && kanjis.contains(b)) {
                throw ParsingFailed(
                    "Do not put $a and $b into the same dont_confuse group, because they're too similar",
                    root
                )
            }
        }

        kanjis.forEachIndexed { idx, kanji ->
            for (followingIdx in idx + 1 ..< kanjis.length) {
                if (kanjis[followingIdx] == kanji) {
                    throw ParsingFailed("Kanji is mentioned more than once in same dont_confuse entry: $kanji", root)
                }
            }

            vocab.dontConfuseKanjis.forEach { haveAlready ->
                if (haveAlready.contains(kanji)) {
                    throw ParsingFailed("Multiple dont_confuse entries mention this kanji: $kanji", root)
                }
            }
        }

        vocab.dontConfuseKanjis.add(kanjis)
    }

    private fun readTopic(root: Element) {
        val en = getMandatoryAttr(root, "name_en")

        checkAttributes(
            root,
            arrayOf(
                "bgColour",
                "HIDDEN",
                "imgSrc",
                "linkHref",
                "linkText",
                "lvl",
                "name_de",
                "name_en",
                "name_fr",
                "name_it",
                "name_ja",
                "notice_de",
                "notice_en",
                "notice_fr",
                "notice_it",
                "notice_ja",
            ),
            getException = { ParsingFailed("Topic $en: $it", root) }
        )

        val topicId = makeTopicId(en)

        if (vocab.topics.any { it.id == topicId }) {
            throw ParsingFailed("Topic id not unique: $topicId", root)
        }

        topic = GoigoiTopic().apply {
            id = topicId
            getMandatoryAttr(root, "name", name)
            imgSrc = getOptionalAttr(root, "imgSrc") ?: ""
            getOptionalAttr(root, "notice", notice)
            linkText = getOptionalAttr(root, "linkText") ?: ""
            linkHref = getOptionalAttr(root, "linkHref") ?: ""
            hidden = getBooleanAttr(root, "HIDDEN")
            bgColour = getOptionalAttr(root, "bgColour") ?: ""

            levels = (getOptionalAttr(root, "lvl") ?: "")
                .split(',')
                .mapNotNull { JLPTLevel.fromString(it.trim()) }
        }

        topic!!.check()
        requireChildless(root)

        vocab.topics.add(topic!!)
    }

    private fun readSubheader(root: Element) {
        val nameEn = getMandatoryAttr(root, "name_en")

        checkAttributes(
            root,
            arrayOf(
                "name_de",
                "name_en",
                "name_fr",
                "name_it",
                "name_ja",
            ),
            getException = { ParsingFailed("Subheader $nameEn: $it", root) }
        )

        subheader.apply {
            de = getOptionalAttr(root, "name_de") ?: ""
            en = nameEn
            fr = getOptionalAttr(root, "name_fr") ?: ""
            it = getOptionalAttr(root, "name_it") ?: ""
            ja = getOptionalAttr(root, "name_ja") ?: ""
        }

        requireChildless(root)
    }

    private fun readUnyt(root: Element) {
        val topic = topic ?: throw ParsingFailed("No topic has been defined!", root)
        val self = this
        val en = getMandatoryAttr(root, "name_en")

        checkAttributes(
            root,
            arrayOf(
                "defaultHint_de",
                "defaultHint_en",
                "defaultHint_fr",
                "defaultHint_it",
                "defaultHint_ja",
                "hasFurigana",
                "hasRomaji",
                "HIDDEN",
                "ignoresCombinedReadings",
                "lvl",
                "name_de",
                "name_en",
                "name_fr",
                "name_it",
                "name_ja",
                "requiresIds",
                "requiresPhrases",
                "requiresSentences",
                "studyLang",
                "translations",
            ),
            getException = { ParsingFailed("Unyt $en: $it", root) }
        )

        val theStudyLang = getMandatoryAttr(root, "studyLang")
        val unytId = makeUnytId(en, theStudyLang)

        if (vocab.findUnytById(unytId) != null) {
            throw ParsingFailed("Unyt id not unique: $unytId", root)
        }

        val unyt = GoigoiUnyt().apply {
            subheader.set(self.subheader)
            self.subheader.clear()

            getMandatoryAttr(root, "name", name)
            getOptionalAttr(root, "defaultHint", defaultHint)

            studyLang = theStudyLang
            id = unytId

            hidden = getBooleanAttr(root, "HIDDEN")
            hasRomaji = getBooleanAttr(root, "hasRomaji")
            hasFurigana = getBooleanAttr(root, "hasFurigana")
            ignoresCombinedReadings = getBooleanAttr(root, "ignoresCombinedReadings")
            requiresSentences = getBooleanAttr(root, "requiresSentences")
            requiresPhrases = getBooleanAttr(root, "requiresPhrases")
            requiresIds = getBooleanAttr(root, "requiresIds")

            levels = (getOptionalAttr(root, "lvl") ?: "")
                .split(',')
                .mapNotNull { JLPTLevel.fromString(it.trim()) }

            requiredTranslations = (getOptionalAttr(root, "translations") ?: "")
                .split(',')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

        topic.unyts.add(unyt)

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "section" -> readSection(tag, unyt)
                else -> throw ParsingFailed("Tag not handled", tag, unyt)
            }
        }

        unyt.check(topic)
    }

    private fun readSection(root: Element, unyt: GoigoiUnyt) {
        checkAttributes(
            root,
            arrayOf(
                "name_de",
                "name_en",
                "name_fr",
                "name_it",
                "name_ja",
            ),
            getException = { ParsingFailed("Section in unyt ${unyt.name.en}: $it", root) }
        )

        val section = GoigoiSection().apply {
            unyt.sections.add(this)
            getOptionalAttr(root, "name", name)
            id = makeSectionId(unyt, name)
        }

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "word" -> readWord(tag, section, unyt)
                else -> throw ParsingFailed("Tag not handled (inside section)", tag, unyt)
            }
        }
    }

    private fun readWord(root: Element, section: GoigoiSection, unyt: GoigoiUnyt) {
        val thePrimaryForm = getMandatoryAttr(root, "w")
        val theRomaji = getOptionalAttr(root, "rom") ?: ""

        checkAttributes(
            root,
            arrayOf(
                "category",
                "common",
                "crossDict",
                "dict",
                "hasCombinedReading",
                "HIDDEN",
                "hint_de",
                "hint_en",
                "hint_fr",
                "hint_it",
                "hint_ja",
                "href",
                "id",
                "Langenscheidt",
                "lvl",
                "origin",
                "rem",
                "rom",
                "studyInContext",
                "tr_de",
                "tr_en",
                "tr_fr",
                "tr_it",
                "tr_ja",
                "usuallyInKana",
                "w",
            ),
            getException = { ParsingFailed("Word $theRomaji: $it", root) }
        )

        val customWordId = getOptionalAttr(root, "id") ?: ""

        if (customWordId.isEmpty() && unyt.requiresIds) {
            throw ParsingFailed("Id for word is missing, but unyt requires ids", root, unyt)
        }

        val wordId = customWordId.let {
            if (it.isEmpty()) {
                makeWordId(thePrimaryForm, theRomaji)
            } else {
                WORD_ID_PREFIX + it
            }
        }

        // Check duplicates here. Further checks are done in compileGoigoi's Main.kt.
        vocab.forEachWordWithId(wordId) { _, otherUnyt, _ ->
            throw ParsingFailed(
                """
                wordId not unique: $wordId
                unyt A: ${unyt.name.en}
                unyt B: ${otherUnyt.name.en}
                """.trimIndent(),
                root,
                unyt
            )
        }

        val theHidden = getBooleanAttr(root, "HIDDEN")

        val theHint = IntlString()
        getOptionalAttr(root, "hint", theHint)

        // A word can suppress the default hint with an empty hint_en.
        if (theHint.isEmpty() && !root.hasAttribute("hint_en")) {
            theHint.set(unyt.defaultHint)
        }

        val theHint2 = WordHint.fromENString(theHint.en)

        if (theHint2 != null) {
            if (theHint.de != theHint2.de && !theHidden && !unyt.hidden) {
                if (theHint2.de == "") {
                    throw CheckFailed(
                        "$wordId: hint_en is ${theHint.en}\n" +
                            "   expected hint_de to be empty\n" +
                            "   got instead: ${theHint.de}",
                        unyt,
                    )
                } else {
                    throw CheckFailed(
                        "$wordId: hint_en is ${theHint.en}\n" +
                            "   expected hint_de to be: ${theHint2.de}\n" +
                            "   actual: ${theHint.de}",
                        unyt
                    )
                }
            }

            theHint.clear()
        }

        val rawStudyInContext = getOptionalAttr(root, "studyInContext") ?: ""

        val theStudyInContext = when (rawStudyInContext) {
            "required" -> StudyInContextKind.REQUIRED
            "preferred" -> StudyInContextKind.PREFERRED
            "" -> StudyInContextKind.NOT_REQUIRED
            else -> throw ParsingFailed("Illegal value for studyInContext: $rawStudyInContext", root, unyt)
        }

        // Initialize the word

        val word = GoigoiWord().apply {
            section.words.add(this)

            id = wordId
            primaryForm = FuriganaString(thePrimaryForm)
            romaji = theRomaji
            translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
            hint = theHint
            hint2 = theHint2
            href = getOptionalAttr(root, "href") ?: ""
            level = JLPTLevel.fromString(getOptionalAttr(root, "lvl") ?: "")
            deLangenscheidt = getOptionalAttr(root, "Langenscheidt") ?: ""
            remark = getOptionalAttr(root, "rem") ?: ""
            dictionaryWord = getOptionalAttr(root, "dict") ?: ""
            hasCustomId = customWordId.isNotEmpty()
            studyInContext = theStudyInContext
            usuallyInKana = getBooleanAttr(root, "usuallyInKana")
            hidden = theHidden
            common = getBooleanAttrOrNull(root, "common")
        }

        // Read categories

        val cats = getOptionalAttr(root, "category")
            ?.takeIf { it.isNotEmpty() }
            ?.split(",")
            ?.map { cat ->
                WordCategory.fromString(cat.trim())
                    ?: throw ParsingFailed(
                        "Unknown category: $cat\n" +
                            "Please use one of: ${WordCategory.entries.joinToString(", ") { it.text }}",
                        root,
                        unyt,
                        word,
                    )
            }

        if (cats != null) {
            word.cats.addAll(cats)
        }

        // Read child nodes

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "synonym" -> readWordSynonym(tag, word, unyt)
                "phrase" -> readPhrase(tag, word, unyt)
                "sentence" -> readSentence(tag, word, unyt)
                "see" -> readSee(tag, word, unyt)
                "keep_apart_from" -> readKeepApartFrom(tag, word, unyt)
                "keep_together" -> readKeepTogether(tag, word, unyt)
                else -> throw ParsingFailed("Tag not handled", tag, unyt, word)
            }
        }

        if (!word.hidden) {
            when {
                unyt.requiresSentences && word.sentences.isEmpty() ->
                    throw ParsingFailed("Unyt requires sentences, word has none!", root, unyt, word)
                unyt.requiresPhrases && word.phrases.isEmpty() ->
                    throw ParsingFailed("Unyt requires phrases, word has none!", root, unyt, word)
            }
        }

        // Check the word

        word.check(
            crossDict = getBooleanAttr(root, "crossDict"),
            hasCombinedReading = getBooleanAttr(root, "hasCombinedReading"),
            unyt = unyt,
        )
    }

    private fun readWordSynonym(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        requireChildless(root)

        checkAttributes(
            root,
            arrayOf("w", "rem"),
            getException = { ParsingFailed("Synonym of word ${word.id}: $it", root) }
        )

        val rawPrimaryForm = getMandatoryAttr(root, "w")

        if (rawPrimaryForm.isEmpty()) {
            throw ParsingFailed("Attribute w of synonym must not be empty", root, unyt, word)
        } else if (rawPrimaryForm == word.primaryForm.raw) {
            throw ParsingFailed("Synonym must not be the same as the word itself", root, unyt, word)
        } else if (rawPrimaryForm == word.primaryForm.kana || rawPrimaryForm == word.primaryForm.kanji) {
            throw ParsingFailed("Synonym must not be the same as the word kana or kanji", root, unyt, word)
        }

        word.synonyms.add(FuriganaString(rawPrimaryForm))
    }

    private fun readPhrase(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        val thePrimaryForm = getOptionalAttr(root, "ph") ?: ""
        val theRomaji = getOptionalAttr(root, "rom") ?: ""

        if (thePrimaryForm.isEmpty()) {
            throw ParsingFailed("Missing mandatory attribute: ph", root, unyt, word)
        }

        if (theRomaji.isEmpty()) {
            throw ParsingFailed("Missing mandatory attribute: rom", root, unyt, word)
        }

        requireChildless(root)

        checkAttributes(
            root,
            arrayOf(
                "allowSpaces",
                "hasDifferentForm",
                "hint",
                "href",
                "explanation_de",
                "explanation_en",
                "explanation_fr",
                "explanation_it",
                "explanation_ja",
                "lvl",
                "origin",
                "ph",
                "rem",
                "rom",
                "tr_de",
                "tr_en",
                "tr_fr",
                "tr_it",
                "tr_ja",
            ),
            getException = { ParsingFailed("Phrase $theRomaji in word ${word.id}: $it", root) }
        )

        if (getOptionalAttr(root, "hint") != null) {
            // Hint is not currently supported. Check ensures we don't confuse hint with rem.
            throw ParsingFailed("Hints are not supported with <phrase>", root, unyt, word)
        }

        val phrase = GoigoiPhrase().apply {
            primaryForm = FuriganaString(thePrimaryForm)
            romaji = theRomaji
            translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
            explanation = IntlString().apply { getOptionalAttr(root, "explanation", this) }
            level = JLPTLevel.fromString(getOptionalAttr(root, "lvl") ?: "")
            hasDifferentForm = getBooleanAttr(root, "hasDifferentForm")
            allowSpaces = getBooleanAttrOrNull(root, "allowSpaces")
            origin = getOptionalAttr(root, "origin") ?: ""
            href = getOptionalAttr(root, "href") ?: ""
            remark = getOptionalAttr(root, "rem") ?: ""
        }

        phrase.checkPhrase(unyt.requiredTranslations, word, unyt)
        word.phrases.add(phrase)
    }

    private fun readSentence(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        val thePrimaryForm = getOptionalAttr(root, "s") ?: ""
        val theRomaji = getOptionalAttr(root, "rom") ?: ""

        if (thePrimaryForm.isEmpty()) {
            throw ParsingFailed("Missing mandatory attribute: s", root, unyt, word)
        }

        if (theRomaji.isEmpty()) {
            throw ParsingFailed("Missing mandatory attribute: rom", root, unyt, word)
        }

        requireChildless(root)

        checkAttributes(
            root,
            arrayOf(
                "allowSpaces",
                "hasDifferentForm",
                "hint",
                "href",
                "explanation_de",
                "explanation_en",
                "explanation_fr",
                "explanation_it",
                "explanation_ja",
                "lvl",
                "origin",
                "rem",
                "rom",
                "s",
                "tr_de",
                "tr_en",
                "tr_fr",
                "tr_it",
                "tr_ja",
            ),
            getException = { ParsingFailed("Sentence $theRomaji in word ${word.id}: $it", root) }
        )

        if (getOptionalAttr(root, "hint") != null) {
            // Hint is not currently supported. Check ensures we don't confuse hint with rem.
            throw ParsingFailed("Hints are not supported with <sentence>", root, unyt, word)
        }

        val sentence = GoigoiPhrase().apply {
            primaryForm = FuriganaString(thePrimaryForm)
            romaji = theRomaji
            translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
            explanation = IntlString().apply { getOptionalAttr(root, "explanation", this) }
            level = JLPTLevel.fromString(getOptionalAttr(root, "lvl") ?: "")
            hasDifferentForm = getBooleanAttr(root, "hasDifferentForm")
            allowSpaces = getBooleanAttrOrNull(root, "allowSpaces")
            origin = getOptionalAttr(root, "origin") ?: ""
            href = getOptionalAttr(root, "href") ?: ""
            remark = getOptionalAttr(root, "rem") ?: ""
        }

        sentence.checkSentence(unyt.requiredTranslations, word, unyt)
        word.sentences.add(sentence)
    }

    private fun readSee(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        val id = getMandatoryAttr(root, "id")

        if (id.isEmpty()) {
            throw ParsingFailed("id of see-also link must not be empty!", root, unyt, word)
        }

        if (WORD_ID_PREFIX + id == word.id) {
            throw ParsingFailed("id of see-also link must not be the word's own id!", root, unyt, word)
        }

        checkAttributes(root, arrayOf("id", "rem")) { ParsingFailed("See-also link in word ${word.id}: $it", root) }

        val link = GoigoiWordLink(
            GoigoiWordLink.Kind.XML_SEE_ALSO,
            wordId = WORD_ID_PREFIX + id,
            remark = getOptionalAttr(root, "rem") ?: ""
        )

        link.check(word, unyt)
        word.links.add(link)

        requireChildless(root)
    }

    private fun readKeepApartFrom(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        val id = getMandatoryAttr(root, "id")

        if (id.isEmpty()) {
            throw ParsingFailed("id of keep_apart_from must not be empty!", root, unyt, word)
        }

        if (WORD_ID_PREFIX + id == word.id) {
            throw ParsingFailed("id of keep_apart_from must not be the word's own id!", root, unyt, word)
        }

        checkAttributes(root, arrayOf("id", "rem")) { ParsingFailed("keep_apart_from in word ${word.id}: $it", root) }

        val link = GoigoiWordLink(
            GoigoiWordLink.Kind.XML_KEEP_APART,
            wordId = WORD_ID_PREFIX + id,
            remark = getOptionalAttr(root, "rem") ?: ""
        )

        link.check(word, unyt)
        word.links.add(link)

        requireChildless(root)
    }

    private fun readKeepTogether(root: Element, word: GoigoiWord, unyt: GoigoiUnyt) {
        val id = getMandatoryAttr(root, "id")

        if (id.isEmpty()) {
            throw ParsingFailed("id of keep_together must not be empty!", root, unyt, word)
        }

        if (WORD_ID_PREFIX + id == word.id) {
            throw ParsingFailed("id of keep_together must not be the word's own id!", root, unyt, word)
        }

        checkAttributes(root, arrayOf("id", "rem")) { ParsingFailed("keep_together in word ${word.id}: $it", root) }

        val link = GoigoiWordLink(
            GoigoiWordLink.Kind.XML_KEEP_TOGETHER,
            wordId = WORD_ID_PREFIX + id,
            remark = getOptionalAttr(root, "rem") ?: ""
        )

        link.check(word, unyt)
        word.links.add(link)

        requireChildless(root)
    }

    private fun makeTopicId(name: String): String {
        return "$TOPIC_ID_PREFIX${name.replace(" ", "")}"
    }

    private fun makeUnytId(name: String, studyLang: String): String {
        // We add "/en" here. This used to be the unyt's translationLang.
        // We still do this in order not to break productive stats.
        return "$UNYT_ID_PREFIX$name($studyLang/en)"
    }

    private fun makeSectionId(unyt: GoigoiUnyt, name: IntlString): String {
        return arrayOf(
            SECTION_ID_PREFIX,
            name.en,
            "@",
            unyt.name.en,
            "(",
            unyt.studyLang,
            ")"
        ).joinToString("")
    }

    private fun makeWordId(primaryForm: String, romaji: String): String {
        // NOTE: When rōmaji is non-empty, we don't take the primary form into account, because we
        // might miss duplicates when the furigana braces are put differently.
        // NOTE: Don't change this method, wordIds should stay stable across app version!

        val id = arrayOf(
            WORD_ID_PREFIX,
            when {
                romaji.isEmpty() -> primaryForm
                else -> romaji
            }
        ).joinToString("")

        return when {
            id.length <= MAX_LENGTH_OF_TEXT_IN_ID -> id
            else -> id.substring(0 ..< MAX_LENGTH_OF_TEXT_IN_ID)
        }
    }

    private fun checkAttributes(
        e: Element,
        recognizedAttrs: Array<String>,
        getException: (msg: String) -> ParsingFailed,
    ) {
        val map = e.attributes

        for (i in 0 ..< map.length) {
            val name = map.item(i).nodeName

            if (!recognizedAttrs.contains(name)) {
                throw getException("Tag uses unknown attribute: $name")
            }
        }
    }

    private fun getOptionalAttr(e: Element, attr: String): String? {
        return if (e.hasAttribute(attr)) {
            e.getAttribute(attr)
        } else {
            null
        }
    }

    private fun getMandatoryAttr(e: Element, attr: String): String {
        return if (e.hasAttribute(attr)) {
            e.getAttribute(attr)
        } else {
            throw ParsingFailed("Missing mandatory attribute $attr", e)
        }
    }

    private fun getOptionalAttr(root: Element, key: String, dst: IntlString) {
        if (getOptionalAttr(root, key) != null) {
            throw ParsingFailed("Attribute $key is deprecated, use ${key}_en instead!", root)
        }

        dst.en = getOptionalAttr(root, "${key}_en") ?: ""
        dst.de = getOptionalAttr(root, "${key}_de") ?: ""
        dst.fr = getOptionalAttr(root, "${key}_fr") ?: ""
        dst.it = getOptionalAttr(root, "${key}_it") ?: ""
        dst.ja = getOptionalAttr(root, "${key}_ja") ?: ""
    }

    private fun getMandatoryAttr(root: Element, key: String, dst: IntlString) {
        val xx = getOptionalAttr(root, key)
        val en = getOptionalAttr(root, "${key}_en")

        if (xx == null && en == null) {
            throw ParsingFailed("Mandatory attribute must have at least a value for English: $key", root)
        }

        getOptionalAttr(root, key, dst)
    }

    private fun getBooleanAttr(e: Element, attr: String) =
        getBooleanAttrOrNull(e, attr) ?: false

    private fun getBooleanAttrOrNull(e: Element, attr: String) =
        if (e.hasAttribute(attr)) {
            val value = e.getAttribute(attr)
            when (value) {
                "yes" -> true
                "true" -> true
                "no" -> false
                "false" -> false
                else -> throw ParsingFailed("Bad value for Boolean attribute: ${attr}=\"${value}\"", e)
            }
        } else {
            null
        }

    private fun forEachChild(root: Node, lambda: (tag: Element) -> Unit) {
        var node = root.firstChild

        while (node != null) {
            if (node.nodeType == Node.ELEMENT_NODE) {
                lambda(node as Element)
            }
            node = node.nextSibling
        }
    }

    private fun requireChildless(element: Element) {
        if (element.firstChild != null) {
            throw ParsingFailed("Tag may not have any child nodes!", element)
        }
    }

    companion object {
        private const val TOPIC_ID_PREFIX = "#"
        private const val UNYT_ID_PREFIX = "="
        private const val SECTION_ID_PREFIX = "*"
        private const val WORD_ID_PREFIX = "-"

        private const val MAX_LENGTH_OF_TEXT_IN_ID = 28
    }
}
