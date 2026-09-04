package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.*
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.ParsingFailed
import io.github.digorydoo.goigoi.core.db.StudyInContextKind
import io.github.digorydoo.goigoi.core.db.WordCategory
import io.github.digorydoo.goigoi.core.db.WordHint
import oracle.xml.parser.v2.DOMParser
import oracle.xml.parser.v2.XMLText
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.InputStream

class GoigoiXmlParser {
    private lateinit var vocab: GoigoiVocab
    private var topic: GoigoiTopic? = null
    private val subheader = IntlString()
    private var filename = ""

    fun parse(stream: InputStream, voc: GoigoiVocab, filename: String) {
        val parser = DOMParser()
        parser.setErrorStream(System.err)
        parser.setValidationMode(DOMParser.NONVALIDATING)
        parser.showWarnings(true)
        parser.parse(stream)

        this.filename = filename
        vocab = voc
        topic = voc.topics.lastOrNull() // unyts are appended to this topic until a new topic is seen
        subheader.clear()

        val root = parser.document.documentElement

        if (root.nodeName != "vocabulary") {
            throw ParsingFailed("XML root is not vocabulary")
        }

        forEachChild(root) { tag ->
            when (tag.nodeName) {
                "kanji" -> readKanji(tag)
                "topic" -> readTopic(tag)
                "subheader" -> readSubheader(tag)
                "unit" -> readUnyt(tag)
                else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
            }
        }

        // Note that voc.check() cannot be called from here, because this function just reads one XML.
        // It will be called from compileGoigoi instead.
    }

    private fun readKanji(root: Element) {
        val errorCtx = "<kanji>"

        try {
            checkAttributes(root, arrayOf())

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "set" -> readKanjiSetTag(tag)
                    "freq" -> readKanjiFreq(tag)
                    "dont_confuse" -> readKanjiDontConfuse(tag)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readKanjiSetTag(root: Element) {
        val errorCtx = "<set>"

        try {
            val lvl = getOptionalAttr(root, "lvl")?.let { JLPTLevel.fromStringOrNull(it) }

            val schoolYear = getOptionalAttr(root, "schoolyear")
                ?.let { it.toIntOrNull() ?: throw ParsingFailed("Value of school year not an int: $it") }
                ?.also { year -> require(year in 1 .. 7) { "Bad value for school year: $year" } }

            checkAttributes(root, arrayOf("lvl", "schoolyear", "rem"))

            val kanjis = root.textContent
                ?.filter { !it.isWhitespace() }
                ?.takeIf { it.isNotEmpty() }
                ?: throw ParsingFailed("Empty value in kanji index tag!")

            val badChars = kanjis.filter { !it.isCJKNotKana() }

            if (badChars.isNotEmpty()) {
                throw ParsingFailed("Tag contains content that aren't kanji: $badChars")
            }

            val numNotNull = arrayOf<Any?>(lvl, schoolYear).filterNotNull().size

            val set = when {
                numNotNull > 1 -> throw ParsingFailed("Tag can have only one of attributes: lvl, schoolyear")
                lvl != null -> vocab.manualKanjiLevels[lvl]
                    ?: mutableSetOf<Char>().also { vocab.manualKanjiLevels[lvl] = it }
                schoolYear != null -> vocab.kanjiBySchoolYear[schoolYear]
                    ?: mutableSetOf<Char>().also { vocab.kanjiBySchoolYear[schoolYear] = it }
                else -> throw ParsingFailed("Tag requires one of attributes: lvl, schoolyear")
            }

            kanjis.forEach { kanji ->
                if (schoolYear != null && set.contains(kanji)) {
                    throw ParsingFailed("Kanji $kanji already in same school year: $schoolYear")
                }
                set.add(kanji)
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readKanjiFreq(root: Element) {
        val errorCtx = "<freq>"

        try {
            checkAttributes(root, arrayOf())

            val kanjis = root.textContent
                ?.filter { !it.isWhitespace() }
                ?.takeIf { it.isNotEmpty() }
                ?: throw ParsingFailed("Empty value in kanji freq tag!")

            val badChars = kanjis.filter { !it.isCJKNotKana() }

            if (badChars.isNotEmpty()) {
                throw ParsingFailed("Tag contains content that aren't kanji: $badChars")
            }

            if (vocab.kanjiByFreq.isNotEmpty()) {
                throw ParsingFailed("Kanji by frequency has already been defined!")
            }

            vocab.kanjiByFreq = kanjis
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readKanjiDontConfuse(root: Element) {
        val errorCtx = "<dont_confuse>"

        try {
            checkAttributes(root, arrayOf())

            val kanjis = root.textContent?.filter { !it.isWhitespace() }

            if (kanjis == null || kanjis.length <= 1) {
                throw ParsingFailed("dont_confuse entry needs to mention at least two kanjis")
            } else if (kanjis.length >= 7) {
                throw ParsingFailed("dont_confuse entry should be split into two: $kanjis")
            }

            val badChars = kanjis.filter { !it.isCJKNotKana() && !it.isKatakana() && !it.isHiragana() }

            if (badChars.isNotEmpty()) {
                throw ParsingFailed("Tag contains content that aren't kanji: $badChars")
            }

            val discouragedKana = kanjis.filter {
                it == 'カ' || it == 'エ' || it == 'ロ' || it == 'タ' || it == 'ニ' || it == 'ハ'
            }

            if (discouragedKana.isNotEmpty()) {
                throw ParsingFailed(
                    "Do not use these kana characters, because there are kanji that look the same: $discouragedKana"
                )
            }

            val badPairs = arrayOf(Pair('末', '未'))

            badPairs.forEach { (a, b) ->
                if (kanjis.contains(a) && kanjis.contains(b)) {
                    throw ParsingFailed(
                        "Do not put $a and $b into the same dont_confuse group, because they're too similar"
                    )
                }
            }

            kanjis.forEachIndexed { idx, kanji ->
                for (followingIdx in idx + 1 ..< kanjis.length) {
                    if (kanjis[followingIdx] == kanji) {
                        throw ParsingFailed("Kanji is mentioned more than once in same dont_confuse entry: $kanji")
                    }
                }

                vocab.dontConfuseKanjis.forEach { haveAlready ->
                    if (haveAlready.contains(kanji)) {
                        throw ParsingFailed("Multiple dont_confuse entries mention this kanji: $kanji")
                    }
                }
            }

            vocab.dontConfuseKanjis.add(kanjis)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readTopic(root: Element) {
        var errorCtx = "<topic>"

        try {
            val nameEn = getMandatoryAttr(root, "name_en")
            errorCtx = "Topic $nameEn"

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
                )
            )

            val topicId = makeTopicId(nameEn)

            if (vocab.topics.any { it.id == topicId }) {
                throw ParsingFailed("Topic id not unique: $topicId")
            }

            val theTopic = GoigoiTopic().apply {
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
                    .mapNotNull { JLPTLevel.fromStringOrNull(it.trim()) }
            }

            errorCtx = "$theTopic"
            topic = theTopic
            vocab.topics.add(theTopic)

            requireChildless(root)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readSubheader(root: Element) {
        val errorCtx = "<subheader>"

        try {
            val nameEn = getMandatoryAttr(root, "name_en")

            checkAttributes(root, arrayOf("name_de", "name_en", "name_fr", "name_it", "name_ja"))

            subheader.apply {
                de = getOptionalAttr(root, "name_de") ?: ""
                en = nameEn
                fr = getOptionalAttr(root, "name_fr") ?: ""
                it = getOptionalAttr(root, "name_it") ?: ""
                ja = getOptionalAttr(root, "name_ja") ?: ""
            }

            requireChildless(root)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readUnyt(root: Element) {
        var errorCtx = "<unit>"
        val unyt: GoigoiUnyt

        try {
            val topic = topic ?: throw ParsingFailed("No topic has been defined!")
            val nameEn = getMandatoryAttr(root, "name_en")
            errorCtx = "Unyt $nameEn"

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
                    "requiresPhrases",
                    "requiresSentences",
                    "studyLang",
                )
            )

            val theStudyLang = getMandatoryAttr(root, "studyLang")
            val unytId = makeUnytId(nameEn, theStudyLang)

            if (vocab.findUnytById(unytId) != null) {
                throw ParsingFailed("Unyt id not unique: $unytId")
            }

            unyt = GoigoiUnyt().apply {
                subheader.set(this@GoigoiXmlParser.subheader)
                this@GoigoiXmlParser.subheader.clear()

                getMandatoryAttr(root, "name", name)
                getOptionalAttr(root, "defaultHint", defaultHint)

                studyLang = theStudyLang
                id = unytId
                filename = this@GoigoiXmlParser.filename

                hidden = getBooleanAttr(root, "HIDDEN")
                hasRomaji = getBooleanAttr(root, "hasRomaji")
                hasFurigana = getBooleanAttr(root, "hasFurigana")
                ignoresCombinedReadings = getBooleanAttr(root, "ignoresCombinedReadings")
                requiresSentences = getBooleanAttr(root, "requiresSentences")
                requiresPhrases = getBooleanAttr(root, "requiresPhrases")

                levels = (getOptionalAttr(root, "lvl") ?: "")
                    .split(',')
                    .mapNotNull { JLPTLevel.fromStringOrNull(it.trim()) }

                // requiredTranslations is now fixed, there is no longer an attribute.
                requiredTranslations = when {
                    hidden -> listOf("en")
                    else -> listOf("en", "de")
                }
            }

            errorCtx = "$unyt"
            topic.unyts.add(unyt)

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "section" -> readSection(tag, unyt)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readSection(root: Element, unyt: GoigoiUnyt) {
        var errorCtx = "<section>"

        try {
            checkAttributes(
                root,
                arrayOf(
                    "name_de",
                    "name_en",
                    "name_fr",
                    "name_it",
                    "name_ja",
                )
            )

            val section = GoigoiSection().apply {
                unyt.sections.add(this)
                getOptionalAttr(root, "name", name)
                id = makeSectionId(unyt, name)
            }

            errorCtx = "" // section is not relevant enough to show it when a child fails

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "word" -> readWord(tag, section, unyt)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readWord(root: Element, section: GoigoiSection, unyt: GoigoiUnyt) {
        var errorCtx = "<word>"

        try {
            val thePrimaryForm = getMandatoryAttr(root, "w")
            val theRomaji = getOptionalAttr(root, "rom") ?: ""
            val customWordId = getOptionalAttr(root, "id") ?: ""
            errorCtx = "Word " + customWordId.ifEmpty { theRomaji }.ifEmpty { thePrimaryForm }

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
                )
            )

            val theHidden = getBooleanAttr(root, "HIDDEN")
            // FIXME move this to CheckGoigoiVocab (use word's hasCustomId: Boolean)
            if (!theHidden && !unyt.hidden && customWordId.isEmpty()) {
                throw CheckFailed("Id for word is missing, word is not hidden")
            }

            val wordId = customWordId.let {
                if (it.isEmpty()) {
                    makeWordId(thePrimaryForm, theRomaji)
                } else {
                    WORD_ID_PREFIX + it
                }
            }

            // FIXME move this to CheckGoigoiVocab
            vocab.forEachWordWithId(wordId) { _, otherUnyt, _ ->
                throw CheckFailed(
                    """
                wordId not unique: $wordId
                unyt A: ${unyt.name.en}
                unyt B: ${otherUnyt.name.en}
                """.trimIndent()
                )
            }

            val theHint = IntlString()
            getOptionalAttr(root, "hint", theHint)

            // A word can suppress the default hint with an empty hint_en.
            if (theHint.isEmpty() && !root.hasAttribute("hint_en")) {
                theHint.set(unyt.defaultHint)
            }

            val theHint2 = WordHint.fromENString(theHint.en)

            if (theHint2 != null) {
                // This check can't be moved into CheckGoigoiVocab, because I want to clear theHint if theHint2 is set.

                if (theHint.de != theHint2.de && !theHidden && !unyt.hidden) {
                    if (theHint2.de == "") {
                        throw CheckFailed(
                            "$wordId: hint_en is ${theHint.en}\n" +
                                "   expected hint_de to be empty\n" +
                                "   got instead: ${theHint.de}"
                        )
                    } else {
                        throw CheckFailed(
                            "$wordId: hint_en is ${theHint.en}\n" +
                                "   expected hint_de to be: ${theHint2.de}\n" +
                                "   actual: ${theHint.de}"
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
                else -> throw ParsingFailed("Illegal value for studyInContext: $rawStudyInContext")
            }

            // Initialize the word

            val word = GoigoiWord().apply {
                id = wordId
                primaryForm = FuriganaString(thePrimaryForm)
                romaji = theRomaji
                translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
                hint = theHint
                hint2 = theHint2
                href = getOptionalAttr(root, "href") ?: ""
                level = JLPTLevel.fromStringOrNull(getOptionalAttr(root, "lvl") ?: "")
                deLangenscheidt = getOptionalAttr(root, "Langenscheidt") ?: ""
                remark = getOptionalAttr(root, "rem") ?: ""
                dictionaryWord = getOptionalAttr(root, "dict") ?: ""
                hasCustomId = customWordId.isNotEmpty()
                studyInContext = theStudyInContext
                usuallyInKana = getBooleanAttr(root, "usuallyInKana")
                hidden = theHidden
                common = getBooleanAttrOrNull(root, "common")
                crossDict = getBooleanAttr(root, "crossDict")
                hasCombinedReading = getBooleanAttr(root, "hasCombinedReading")
            }

            errorCtx = "$word"
            section.words.add(word)

            // Read categories

            val cats = getOptionalAttr(root, "category")
                ?.takeIf { it.isNotEmpty() }
                ?.split(",")
                ?.map { cat ->
                    WordCategory.fromString(cat.trim())
                        ?: throw ParsingFailed(
                            "Unknown category: $cat\n" +
                                "Please use one of: ${WordCategory.entries.joinToString(", ") { it.text }}",
                        )
                }

            if (cats != null) {
                word.cats.addAll(cats)
            }

            // Read child nodes

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "synonym" -> readWordSynonym(tag, word)
                    "phrase" -> readPhrase(tag, word)
                    "sentence" -> readSentence(tag, word)
                    "see" -> readSee(tag, word)
                    "keep_apart_from" -> readKeepApartFrom(tag, word)
                    "keep_together" -> readKeepTogether(tag, word)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }

            if (!word.hidden) {
                when {
                    unyt.requiresSentences && word.sentences.isEmpty() ->
                        throw ParsingFailed("Unyt requires sentences, word has none!")
                    unyt.requiresPhrases && word.phrases.isEmpty() ->
                        throw ParsingFailed("Unyt requires phrases, word has none!")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readWordSynonym(root: Element, word: GoigoiWord) {
        val errorCtx = "<synonym>"

        try {
            requireChildless(root)
            checkAttributes(root, arrayOf("w", "rem"))

            val rawPrimaryForm = getMandatoryAttr(root, "w")

            if (rawPrimaryForm.isEmpty()) {
                throw ParsingFailed("Attribute w of synonym must not be empty")
            } else if (rawPrimaryForm == word.primaryForm.raw) {
                throw ParsingFailed("Synonym must not be the same as the word itself")
            } else if (rawPrimaryForm == word.primaryForm.kana || rawPrimaryForm == word.primaryForm.kanji) {
                throw ParsingFailed("Synonym must not be the same as the word kana or kanji")
            }

            word.synonyms.add(FuriganaString(rawPrimaryForm))
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readPhrase(root: Element, word: GoigoiWord) {
        var errorCtx = "<phrase>"

        try {
            val thePrimaryForm = getMandatoryAttr(root, "ph") // TODO getMandatoryNonEmptyAttr
            val theRomaji = getMandatoryAttr(root, "rom")
            errorCtx = "Phrase " + theRomaji.ifEmpty { thePrimaryForm }

            if (thePrimaryForm.isEmpty()) {
                throw ParsingFailed("Missing mandatory attribute: ph")
            }

            if (theRomaji.isEmpty()) {
                throw ParsingFailed("Missing mandatory attribute: rom")
            }

            checkAttributes(
                root,
                arrayOf(
                    "allowSpaces",
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
                )
            )

            if (getOptionalAttr(root, "hint") != null) {
                // Hint is not currently supported. Check ensures we don't confuse hint with rem.
                throw ParsingFailed("Hints are not supported with <phrase>")
            }

            val phrase = GoigoiPhraseOrSentence(GoigoiPhraseOrSentence.Kind.PHRASE).apply {
                primaryForm = FuriganaString(thePrimaryForm)
                romaji = theRomaji
                translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
                explanation = IntlString().apply { getOptionalAttr(root, "explanation", this) }
                level = JLPTLevel.fromStringOrNull(getMandatoryAttr(root, "lvl"))
                allowSpaces = getBooleanAttrOrNull(root, "allowSpaces")
                origin = getOptionalAttr(root, "origin") ?: ""
                href = getOptionalAttr(root, "href") ?: ""
                remark = getOptionalAttr(root, "rem") ?: ""
            }

            word.phrases.add(phrase)

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "ask" -> readWordFormToAsk(tag, phrase, word)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readSentence(root: Element, word: GoigoiWord) {
        var errorCtx = "<sentence>"

        try {
            val thePrimaryForm = getMandatoryAttr(root, "s") // TODO getMandatoryNonEmptyAttr
            val theRomaji = getMandatoryAttr(root, "rom")
            errorCtx = "Sentence " + theRomaji.ifEmpty { thePrimaryForm }

            if (thePrimaryForm.isEmpty()) {
                throw ParsingFailed("Missing mandatory attribute: s")
            }

            if (theRomaji.isEmpty()) {
                throw ParsingFailed("Missing mandatory attribute: rom")
            }

            checkAttributes(
                root,
                arrayOf(
                    "allowSpaces",
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
                )
            )

            if (getOptionalAttr(root, "hint") != null) {
                // Hint is not currently supported. Check ensures we don't confuse hint with rem.
                throw ParsingFailed("Hints are not supported with <sentence>")
            }

            val sentence = GoigoiPhraseOrSentence(GoigoiPhraseOrSentence.Kind.SENTENCE).apply {
                primaryForm = FuriganaString(thePrimaryForm)
                romaji = theRomaji
                translation = IntlString().apply { getMandatoryAttr(root, "tr", this) }
                explanation = IntlString().apply { getOptionalAttr(root, "explanation", this) }
                level = JLPTLevel.fromStringOrNull(getMandatoryAttr(root, "lvl"))
                allowSpaces = getBooleanAttrOrNull(root, "allowSpaces")
                origin = getOptionalAttr(root, "origin") ?: ""
                href = getOptionalAttr(root, "href") ?: ""
                remark = getOptionalAttr(root, "rem") ?: ""
            }

            word.sentences.add(sentence)

            // Later, we might extend this to break down the entire sentence:
            // <ignore w="【田：た】【中：なか】さん、お" />
            // <ask w="【昼：ひる】ごはん" />
            // <ignore w="、" />
            // <ref w="【食：た】べ" id="taberu-eat" />
            // <ignore w="に" />
            // <ref ="【行：い】かない" id="iku-go" />
            // <ignore w="？" />

            forEachChild(root) { tag ->
                when (tag.nodeName) {
                    "ask" -> readWordFormToAsk(tag, sentence, word)
                    else -> throw ParsingFailed("Tag not handled: <${tag.nodeName}>")
                }
            }
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readWordFormToAsk(root: Element, phraseOrSentence: GoigoiPhraseOrSentence, word: GoigoiWord) {
        var errorCtx = "Word form"

        try {
            val w = getOptionalAttr(root, "w")
            val stem = getOptionalAttr(root, "stem")
            val suffix = getOptionalAttr(root, "suffix")

            if (w != null) {
                if (stem != null || suffix != null) {
                    throw ParsingFailed("Word form cannot have both w and stem/suffix")
                }

                if (w.isEmpty()) {
                    throw ParsingFailed("Word form: w cannot be empty when defined")
                }

                if (w != w.trim()) {
                    throw ParsingFailed("Word form is not properly trimmed: '$w'")
                }

                errorCtx += " ($w)"
            } else {
                if (stem == null || suffix == null) {
                    throw ParsingFailed("Word form needs to have either w or both stem and suffix")
                }

                if (stem.isEmpty()) {
                    throw ParsingFailed("Word form: stem cannot be empty when defined")
                }

                if (suffix.isEmpty()) {
                    throw ParsingFailed("Word form: suffix cannot be empty when defined")
                }

                if (stem != stem.trim()) {
                    throw ParsingFailed("Word form: stem is not properly trimmed")
                }

                if (suffix != suffix.trim()) {
                    throw ParsingFailed("Word form: suffix is not properly trimmed")
                }

                errorCtx += " ($stem + $suffix)"
            }

            checkAttributes(root, arrayOf("w", "stem", "suffix"))
            requireChildless(root)

            if (phraseOrSentence.wordFormToAsk.isNotEmpty()) {
                throw ParsingFailed("The <ask> tag can only appear once")
            }

            phraseOrSentence.wordFormToAsk = FuriganaString(w ?: stem ?: "")
            phraseOrSentence.wordFormToAskSuffix = suffix ?: ""
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readSee(root: Element, word: GoigoiWord) {
        val errorCtx = "<see>"

        try {
            val id = getMandatoryAttr(root, "id")

            if (id.isEmpty()) {
                throw ParsingFailed("id of see-also link must not be empty!")
            }

            if (WORD_ID_PREFIX + id == word.id) {
                throw ParsingFailed("id of see-also link must not be the word's own id!")
            }

            checkAttributes(root, arrayOf("id", "rem"))

            val link = GoigoiWordLink(
                GoigoiWordLink.Kind.XML_SEE_ALSO,
                wordId = WORD_ID_PREFIX + id,
                remark = getOptionalAttr(root, "rem") ?: ""
            )

            word.links.add(link)
            requireChildless(root)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readKeepApartFrom(root: Element, word: GoigoiWord) {
        val errorCtx = "<keep_apart_from>"

        try {
            val id = getMandatoryAttr(root, "id")

            if (id.isEmpty()) {
                throw ParsingFailed("id of keep_apart_from must not be empty!")
            }

            if (WORD_ID_PREFIX + id == word.id) {
                throw ParsingFailed("id of keep_apart_from must not be the word's own id!")
            }

            checkAttributes(root, arrayOf("id", "rem"))

            val link = GoigoiWordLink(
                GoigoiWordLink.Kind.XML_KEEP_APART,
                wordId = WORD_ID_PREFIX + id,
                remark = getOptionalAttr(root, "rem") ?: ""
            )

            word.links.add(link)
            requireChildless(root)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    private fun readKeepTogether(root: Element, word: GoigoiWord) {
        val errorCtx = "<keep_together>"

        try {
            val id = getMandatoryAttr(root, "id")

            if (id.isEmpty()) {
                throw ParsingFailed("id of keep_together must not be empty!")
            }

            if (WORD_ID_PREFIX + id == word.id) {
                throw ParsingFailed("id of keep_together must not be the word's own id!")
            }

            checkAttributes(root, arrayOf("id", "rem"))

            val link = GoigoiWordLink(
                GoigoiWordLink.Kind.XML_KEEP_TOGETHER,
                wordId = WORD_ID_PREFIX + id,
                remark = getOptionalAttr(root, "rem") ?: ""
            )

            word.links.add(link)
            requireChildless(root)
        } catch (e: Exception) {
            rethrow(e, errorCtx)
        }
    }

    companion object {
        private const val TOPIC_ID_PREFIX = "#"
        private const val UNYT_ID_PREFIX = "="
        private const val SECTION_ID_PREFIX = "*"
        private const val WORD_ID_PREFIX = "-"

        private const val MAX_LENGTH_OF_TEXT_IN_ID = 28

        private fun rethrow(e: Throwable, errorCtx: String): Nothing {
            val newMsg = when {
                errorCtx.isEmpty() -> e.message // error context is suppressed, e.g. <section>
                else -> "$errorCtx:\n${e.message?.prependIndent("   ")}"
            }
            throw ParsingFailed(newMsg, e)
        }

        private fun makeTopicId(name: String): String {
            return "$TOPIC_ID_PREFIX${name.replace(" ", "")}"
        }

        private fun makeUnytId(name: String, studyLang: String): String {
            // Add "/en" here. This used to be the unyt's translationLang.
            // I still do this in order not to break productive stats.
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

        private fun checkAttributes(e: Element, recognizedAttrs: Array<String>) {
            val map = e.attributes

            for (i in 0 ..< map.length) {
                val name = map.item(i).nodeName

                if (!recognizedAttrs.contains(name)) {
                    throw ParsingFailed("Tag uses unknown attribute: $name")
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
                throw ParsingFailed("Missing mandatory attribute $attr")
            }
        }

        private fun getOptionalAttr(root: Element, key: String, dst: IntlString) {
            if (getOptionalAttr(root, key) != null) {
                throw ParsingFailed("Attribute $key is deprecated, use ${key}_en instead!")
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
                throw ParsingFailed("Mandatory attribute must have at least a value for English: $key")
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
                    else -> throw ParsingFailed("Bad value for Boolean attribute: ${attr}=\"${value}\"")
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

        private fun requireChildless(element: Element, allowText: Boolean = false) {
            var node = element.firstChild

            while (node != null) {
                if (!allowText || node !is XMLText) {
                    throw ParsingFailed("Unexpected child node: $node")
                }
                node = node.nextSibling
            }
        }
    }
}
