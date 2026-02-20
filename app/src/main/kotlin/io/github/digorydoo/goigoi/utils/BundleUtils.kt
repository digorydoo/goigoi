package io.github.digorydoo.goigoi.utils

import android.os.Build
import android.os.Bundle
import java.io.Serializable

fun Bundle.getIntOrNull(key: String) = when {
    containsKey(key) -> getInt(key)
    else -> null
}

fun Bundle.getStringOrNull(key: String) = when {
    containsKey(key) -> getString(key)
    else -> null
}

@Suppress("DEPRECATION")
inline fun <reified T: Serializable> Bundle.getSerializableOrNull(key: String): T? = when {
    containsKey(key) -> when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
        else -> getSerializable(key) as? T
    }
    else -> null
}
