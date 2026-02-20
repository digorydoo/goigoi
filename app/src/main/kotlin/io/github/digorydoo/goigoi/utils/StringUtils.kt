package io.github.digorydoo.goigoi.utils

import io.github.digorydoo.goigoi.BuildConfig
import ch.digorydoo.kutils.cjk.IntlString
import kotlin.random.Random

@Suppress("KotlinConstantConditions")
val IntlString.withStudyLang
    get() = when (BuildConfig.FLAVOR) {
        "japanese_free" -> ja
        "french_free" -> fr
        else -> throw RuntimeException("Unhandled build flavor: ${BuildConfig.FLAVOR}")
    }

object StringUtils {
    /**
     * @return A random subset of the given collection with the desired count
     */
    fun getRandomSubset(
        chars: Collection<Char>,
        desiredCount: Int,
        except: Set<Char>,
        ignoreCase: Boolean = false,
    ): Set<Char> {
        val result = chars
            .filterNot { c ->
                if (ignoreCase) {
                    val lc = c.lowercaseChar()
                    except.any { it.lowercaseChar() == lc }
                } else {
                    except.contains(c)
                }
            }
            .toMutableSet()

        while (result.size > desiredCount) {
            val idx = Random.nextInt(0, result.size)
            result.remove(result.elementAt(idx))
        }

        return result
    }
}
