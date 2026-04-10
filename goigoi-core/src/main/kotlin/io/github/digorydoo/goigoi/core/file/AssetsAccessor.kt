package io.github.digorydoo.goigoi.core.file

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface AssetsAccessor {
    val filesDir: File
    fun useAsset(path: String, lambda: (InputStream) -> Unit)
    fun usePrivateFileInput(path: String, lambda: (InputStream) -> Unit)
    fun usePrivateFileOutput(path: String, lambda: (OutputStream) -> Unit)
    fun useDownloadFileOutput(filename: String, lambda: (OutputStream) -> Unit)
}
