package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.FileNotFoundException

object BitmapPool {
    private const val TAG = "BitmapPool"

    private val loadedBitmaps = mutableMapOf<String, Bitmap?>()

    fun getFromAssets(path: String, ctx: Context): Bitmap? {
        if (loadedBitmaps.containsKey(path)) {
            return loadedBitmaps[path]
        } else {
            var bmp: Bitmap? = null

            try {
                val stream = ctx.assets.open(path)
                val bytes = stream.readBytes()
                stream.close()
                val opts = BitmapFactory.Options()
                bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            } catch (_: FileNotFoundException) {
                Log.e(TAG, "File not found: $path")
            }

            loadedBitmaps[path] = bmp
            return bmp
        }
    }
}
