package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink

fun GoigoiWordLink.check(word: GoigoiWord, unyt: GoigoiUnyt) {
    // Check that the link doesn't point to its own word

    if (wordId == word.id) {
        throw CheckFailed("Link must not refer to its own word!", unyt, word)
    }

    if (kind == GoigoiWordLink.Kind.XML_SEE_ALSO) {
        // Check see-also link's remark.

        val allowedRem = arrayOf(
            "v.i.",
            "v.t.",
            "noun",
            "verb",
            "adjective",
            "closely related",
            "antonym",
            "not auto-generated due to honorific prefix"
        )

        if (remark.isEmpty()) {
            throw CheckFailed(
                "See-also link has no remark!\n  But we expected one of: ${allowedRem.joinToString(", ")}",
                unyt,
                word,
            )
        } else if (!allowedRem.contains(remark)) {
            throw CheckFailed(
                "See-also link has remark: ${remark}\n  But we expected one of: ${allowedRem.joinToString(", ")}",
                unyt,
                word
            )
        }

        val checkHint = when (remark) {
            "v.i." -> "v.t."
            "v.t." -> "v.i."
            else -> ""
        }

        if (checkHint.isNotEmpty() && !word.hint.en.contains(checkHint) && word.hint2?.en != checkHint) {
            throw CheckFailed(
                "See-also link (rem=${remark}) requires that its own word be marked as $checkHint in hint_en",
                unyt,
                word
            )
        }
    }

    // We can't access the other word yet. Checks related to the other word are done in CheckGoigoiVocab.
}
