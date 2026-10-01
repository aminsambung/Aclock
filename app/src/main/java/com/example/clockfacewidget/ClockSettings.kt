package com.example.clockfacewidget

import android.content.Context

enum class ClockFace(val label: String) {
    BLACK("Classic Black"),
    WHITE("Classic White"),
    SPACE("Space"),
    ROMAN("Roman")
}

object ClockSettings {
    private const val PREF = "clock_settings"
    private const val FACE = "face"
    private const val SECONDS = "seconds"

    fun face(context: Context): ClockFace {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(FACE, ClockFace.BLACK.name) ?: ClockFace.BLACK.name
        return runCatching { ClockFace.valueOf(value) }.getOrDefault(ClockFace.BLACK)
    }

    fun seconds(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(SECONDS, false)

    fun save(context: Context, face: ClockFace, seconds: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(FACE, face.name)
            .putBoolean(SECONDS, seconds)
            .apply()
    }
}
