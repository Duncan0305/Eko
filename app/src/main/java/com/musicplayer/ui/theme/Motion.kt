package com.musicplayer.ui.theme

import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect

object EkoMotion {
    const val Press = 140
    const val Enter = 240
    const val Exit = 160
    const val SectionEnter = 280
    const val SectionStagger = 45
    const val Player = 320
    const val PlayerDismiss = 240
    const val WelcomePulse = 2_400
    const val WelcomeOrbit = 12_000
}

/** Centraliza el respeto a la preferencia de animaciones reducidas del sistema. */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    fun disabled() = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    var reduced by remember { mutableStateOf(disabled()) }
    LifecycleResumeEffect(Unit) {
        reduced = disabled()
        onPauseOrDispose { }
    }
    return reduced
}
