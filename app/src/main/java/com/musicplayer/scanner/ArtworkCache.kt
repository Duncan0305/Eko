package com.musicplayer.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * Carátulas direccionadas por contenido.
 *
 * Guarda una copia en **alta resolución** para que la carátula del reproductor, el fondo a
 * pantalla completa y la notificación del sistema se mantengan nítidas en pantallas de alta
 * densidad. Las miniaturas de la lista no se pre-reducen aquí: Coil reduce el archivo guardado
 * al tamaño exacto que necesita cada componente. Los originales nunca llegan a la UI ni a la
 * base de datos.
 */
class ArtworkCache(context: Context) {
    private val directory = File(context.filesDir, "artwork")
    private var bytesUsed = 0L

    fun store(bytes: ByteArray): String? {
        directory.mkdirs()
        if (bytes.size > MaxInputBytes) return null
        val key = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        val target = File(directory, "$key.webp")
        if (target.isFile) return target.absolutePath
        if (bytesUsed >= MaxCacheBytes) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Decodifica con una muestra potencia de dos que conserve al menos MaxEdge y
        // luego escala al tamaño exacto (evita el recorte a la baja que causaba el borroso).
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
        val scaled = scaleToMaxEdge(decoded)
        if (scaled !== decoded) decoded.recycle()

        val temporary = File(directory, "$key.tmp")
        try {
            temporary.outputStream().use { scaled.compress(webpFormat, WebpQuality, it) }
            check(temporary.renameTo(target))
            bytesUsed += target.length()
        } finally {
            scaled.recycle()
            temporary.delete()
        }
        return target.absolutePath
    }

    fun prune(referenced: Set<String>) {
        directory.listFiles()?.forEach { if (it.absolutePath !in referenced) it.delete() }
        bytesUsed = directory.listFiles()?.sumOf { it.length() } ?: 0
    }

    /** Muestra de decodificación: el resultado nunca baja de [MaxEdge] en el lado mayor. */
    private fun sampleSizeFor(width: Int, height: Int): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= MaxEdge) sample *= 2
        return sample
    }

    /** Reduce proporcionalmente a [MaxEdge] sin ampliar imágenes más pequeñas. */
    private fun scaleToMaxEdge(bitmap: Bitmap): Bitmap {
        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest <= MaxEdge) return bitmap
        val ratio = MaxEdge.toFloat() / longest
        val width = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val height = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private companion object {
        /** Lado mayor guardado en disco. Cubre la carátula del reproductor (~420 dp) en pantallas 3x. */
        const val MaxEdge = 1280
        const val WebpQuality = 92
        const val MaxInputBytes = 16 * 1024 * 1024
        const val MaxCacheBytes = 128L * 1024 * 1024

        @Suppress("DEPRECATION")
        val webpFormat: Bitmap.CompressFormat =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
            else Bitmap.CompressFormat.WEBP
    }
}
