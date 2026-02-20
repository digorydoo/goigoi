package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import ch.digorydoo.kutils.cjk.FuriganaString

class Surname(
    val suffices: Array<String>,
    val suffixRequired: Boolean,
    val prefixes: Array<String>,
    val knownFirstname: String? = null,
)

class Firstname(val suffix: String)
enum class SurnameUsage { SUFFIX, FIRSTNAME }

val knownSurnames = mapOf(
    "Aoki" to Surname(arrayOf("san", "sensei"), suffixRequired = true, arrayOf("Mrs", "Professor")),
    "Furukawa" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mrs")),
    "Ikeda" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Ms")),
    "Ishikawa" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Kaneko" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Kimura" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Miss")),
    "Kobayashi" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Koyama" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mrs")),
    "Matsumoto" to Surname(arrayOf("san"), suffixRequired = true, arrayOf("Mr")),
    "Morita" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Nakagawa" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Ms")),
    "Nakamura" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mrs")),
    "Nishimura" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Ogawa" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr"), "Ken"),
    "Takagi" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Takahashi" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Miss")),
    "Tanaka" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr", "Mrs", "Ms")),
    "Taniguchi" to Surname(arrayOf("san", "sama"), suffixRequired = false, arrayOf("Mrs")),
    "Yamada" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
    "Yamaguchi" to Surname(arrayOf("san", "sensei"), suffixRequired = true, arrayOf("Mr", "Professor")),
    "Yamashita" to Surname(arrayOf("san", "sama"), suffixRequired = true, arrayOf("Mr")),
)

val knownFirstnames = mapOf(
    "Naomi" to Firstname("san"),
    "Haruto" to Firstname("san"),
    "Kaoru" to Firstname("san"),
    "Yoshi" to Firstname("san"),
    "Miki" to Firstname("san"),
    "Mari" to Firstname("san"),
    "Reo" to Firstname("kun"),
    "Risa" to Firstname("chan"),
)

val allowedOrigins = arrayOf(
    Regex("self"),
    Regex("500mon(| N3| N2| N1) q[0-9.]*(|, q[0-9.]*)(| modified)"),
    Regex("BondLingo(| modified)"),
    Regex("ChatGPT"),
    Regex("Doraemon [0-9]+, p\\.[0-9]+(| modified)"), // too tedious to type: ドラえもん第２巻、20頁
    Regex("GENKI[12](| p\\.[0-9]*)(| modified)"),
    Regex("Kaname Naito(| modified)"),
    Regex("Langenscheidt(| p\\.[0-9]+)"),
    Regex("Miku(| modified)"),
    Regex("Oxford p\\.[0-9]+(| modified)"),
    Regex("Yuko Sensei(| modified)"),
    Regex("duolingo(| modified)"),
    Regex("hinative(| modified)"),
    Regex("jpod101(| audio)(| modified)"),
    Regex("lib0(| modified)"),
    Regex("tofugu(| modified)"),
)

val supportedLanguages = arrayOf("en", "de", "fr", "it", "ja")

/**
 * Computes a rōmaji form from the given primary form including any defined furigana, and checks
 * if it matches the given rōmaji.
 *
 * NOTE: We do NOT allow leaving XML rōmaji empty when furigana is given, because on the one hand,
 * the computed rōmaji isn't perfect (no space, for instance), and on the other hand, we need the
 * XML rōmaji when the XML is imported in japanese-wotd!
 */
fun checkRomaji(primaryForm: String, givenRomaji: String, unyt: GoigoiUnyt, word: GoigoiWord) {
    val computedRomaji = FuriganaString(primaryForm).romaji

    val ck1 = givenRomaji
        .lowercase()
        .replace("ī", "ii") // to allow both pīman, oishii
        .replace(Regex("n-([aeiou])"), "n'$1") // e.g. san-en -> san'en
        .replace("-", "") // o-kashi or okashi
        .replace(" ", "")
        .replace("?", ".")

    val ck2 = computedRomaji
        .lowercase()
        .replace("ī", "ii")
        .replace(Regex("n-([aeiou])"), "n'$1")
        .replace("-", "")
        .replace(" ", "")
        .replace("?", ".")

    if (ck1 != ck2) {
        throw CheckFailed(
            arrayOf(
                "\nRōmaji forms do not match!",
                "XML w:              $primaryForm",
                "XML rom:            $givenRomaji",
                "Computed:           $computedRomaji",
                "Check 1 (XML):      $ck1",
                "Check 2 (computed): $ck2",
            ).joinToString("\n"),
            unyt,
            word,
        )
    }
}
