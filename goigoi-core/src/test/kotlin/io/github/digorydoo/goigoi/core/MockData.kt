package io.github.digorydoo.goigoi.core

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.AnswerAccuracy
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.Word

fun getMockWordTobidasu() = Word().apply {
    primaryForm = FuriganaString("【飛：と】び【出：だ】す")
    romaji = "tobidasu"
    translation.en = "to rush out"
    hint.en = "v.t. & i."
    level = JLPTLevel.N3
    usuallyInKana = false

    acceptableAnswers.apply {
        add(AcceptableAnswer("飛出す", AnswerAccuracy.ALT_SPELLING_SAME_WORD))

        add(AcceptableAnswer("出す", AnswerAccuracy.INACCURATE))
        add(AcceptableAnswer("だす", AnswerAccuracy.INACCURATE))

        // This alternative verb should not propagate to phrases and sentences.
        add(AcceptableAnswer("駆けつける", AnswerAccuracy.CORRECT_FOR_TRANSLATION))
        add(AcceptableAnswer("かけつける", AnswerAccuracy.CORRECT_FOR_TRANSLATION))

        // This isn't a real Japanese word, but we add it for the sake of the test.
        // We allow it to propagate to phrases and sentences.
        add(AcceptableAnswer("ラッシュアウトする", AnswerAccuracy.CORRECT_IN_PHRASES_AND_SENTENCES))
    }

    phrases.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("【急：きゅう】に【飛：と】び【出：だ】す")
            romaji = "kyū ni tobidasu"
            translation.en = "to suddenly rush out"
            explanation.en = "sample explanation"
            level = JLPTLevel.N3
        }
    )
    sentences.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("【猫：ねこ】が【飛：と】び【出：だ】した。")
            romaji = "Neko ga tobidashita."
            translation.en = "A cat rushed out."
            level = JLPTLevel.N3
            explanation.en = "sample explanation"

            wordFormToAsk = FuriganaString("【飛：と】び【出：だ】した")
            acceptableAnswers.add(AcceptableAnswer("飛出した", AnswerAccuracy.CORRECT))
        }
    )
}

fun getMockWordMazui() = Word().apply {
    primaryForm = FuriganaString("【不味：まず】い")
    romaji = "mazui"
    translation.en = "unpleasant taste"
    hint.en = "i-adjective"
    level = JLPTLevel.N5
    usuallyInKana = true

    // マズい isn't a common spelling, but we add it for the sake of this test
    acceptableAnswers.add(AcceptableAnswer("マズい", AnswerAccuracy.ALT_SPELLING_SAME_WORD))

    phrases.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("まずくて【食：た】べられない")
            romaji = "mazukute taberarenai"
            translation.en = "so bad-tasting you cannot eat it"
            level = JLPTLevel.N4

            wordFormToAsk = FuriganaString("まずくて")
            acceptableAnswers.apply {
                // The phrase needs to redeclare the forms, because the word does not appear in dictionary form.
                add(AcceptableAnswer("不味くて", AnswerAccuracy.CORRECT))
                add(AcceptableAnswer("マズくて", AnswerAccuracy.CORRECT))
            }
        }
    )
    sentences.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("この【料：りょう】【理：り】は まずい。")
            romaji = "Kono ryōri wa mazui."
            translation.en = "This dish tastes bad."
            level = JLPTLevel.N5
        }
    )
}

fun getMockWordEgaku() = Word().apply {
    primaryForm = FuriganaString("【描：えが】く")
    romaji = "egaku"
    translation.en = "to draw"
    hint.en = "v.t."
    level = JLPTLevel.N3

    acceptableAnswers.apply {
        add(AcceptableAnswer("かく", AnswerAccuracy.ALT_READING_SAME_WORD))
        add(AcceptableAnswer("塗り替える", AnswerAccuracy.INACCURATE))
        add(AcceptableAnswer("ぬりかえる", AnswerAccuracy.INACCURATE))
    }

    phrases.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("【絵：え】を【描：えが】く")
            romaji = "e o egaku"
            translation.en = "to paint a picture"
            level = JLPTLevel.N3
        }
    )
    sentences.add(
        PhraseOrSentence().apply {
            primaryForm = FuriganaString("【将：しょう】【来：らい】の【夢：ゆめ】を【心：こころ】に【描：えが】いている。")
            romaji = "Shōrai no yume o kokoro ni egaite iru."
            translation.en = "I'm picturing my future dreams in my mind."
            level = JLPTLevel.N3

            wordFormToAsk = FuriganaString("【描：えが】いて")
            acceptableAnswers.add(AcceptableAnswer("えがいて", AnswerAccuracy.ALT_READING_SAME_WORD))
        }
    )
}
