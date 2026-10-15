package com.musicplayer.data.model

/**
 * Reproducciones acumuladas de una canción.
 *
 * La clave es el `uri` del documento, que se mantiene estable entre reescaneos y cambios de
 * carpeta, de modo que las estadísticas de uso sobreviven a la regeneración del índice.
 */
data class PlayRecord(val playCount: Int, val lastPlayedAt: Long)
