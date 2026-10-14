package com.musicplayer.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.security.MessageDigest

/** Small, content-addressed thumbnails. Originals never enter the UI or database. */
class ArtworkCache(context: Context) {
    private val directory = File(context.filesDir, "artwork")
    private var bytesUsed = 0L

    fun store(bytes: ByteArray): String? {
        directory.mkdirs()
        if (bytes.size > 16 * 1024 * 1024) return null
        val key = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        val target = File(directory, "$key.jpg")
        if (target.isFile) return target.absolutePath
        if (bytesUsed >= 96L * 1024 * 1024) return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        var sample = 1
        while (maxOf(options.outWidth, options.outHeight) / sample > 384) sample *= 2
        options.inJustDecodeBounds = false
        options.inSampleSize = sample
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
        val temporary = File(directory, "$key.tmp")
        try {
            temporary.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            check(temporary.renameTo(target))
            bytesUsed += target.length()
        } finally {
            bitmap.recycle()
            temporary.delete()
        }
        return target.absolutePath
    }

    fun prune(referenced: Set<String>) {
        directory.listFiles()?.forEach { if (it.absolutePath !in referenced) it.delete() }
        bytesUsed = directory.listFiles()?.sumOf { it.length() } ?: 0
    }
}
