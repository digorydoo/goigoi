package io.github.digorydoo.goigoi.utils

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import io.github.digorydoo.goigoi.core.file.AssetsAccessor
import java.io.InputStream
import java.io.OutputStream

class AndroidAssetsAccessor(private val getContext: () -> Context): AssetsAccessor {
    override val filesDir = getContext().filesDir!!

    override fun useAsset(path: String, lambda: (InputStream) -> Unit) {
        getContext().assets.open(path).use(lambda)
    }

    override fun usePrivateFileInput(path: String, lambda: (InputStream) -> Unit) {
        getContext().openFileInput(path).use(lambda)
    }

    override fun usePrivateFileOutput(path: String, lambda: (OutputStream) -> Unit) {
        getContext().openFileOutput(path, Context.MODE_PRIVATE).use(lambda)
    }

    override fun useDownloadFileOutput(filename: String, lambda: (OutputStream) -> Unit) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val ctx = getContext()
        val resolver = ctx.contentResolver

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        if (uri == null) {
            Log.w(TAG, "Failed to resolve: $filename")
            return
        }

        val stream = resolver.openOutputStream(uri)

        if (stream == null) {
            Log.w(TAG, "Failed to open: $filename")
            return
        }

        stream.use(lambda)
    }

    companion object {
        private const val TAG = "AndroidAssetsAccessor"
    }
}
