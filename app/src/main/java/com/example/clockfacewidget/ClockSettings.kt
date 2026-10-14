package com.example.clockfacewidget

import android.content.Context

enum class ClockFace(val label: String) {
    BLACK("Classic Black"),
    WHITE("Classic White"),
    SPACE("Space"),
    ROMAN("Roman")
}

// TAMBAHAN: Enum untuk pilihan gaya jam digital
enum class DigitalStyle(val label: String) {
    NONE("Tanpa Jam Digital"),
    HOUR_24("Format 24 Jam"),
    HOUR_12("Format 12 Jam (AM/PM)")
}

object ClockSettings {
    private const val PREF = "clock_settings"
    private const val FACE = "face"
    private const val SECONDS = "seconds"
    private const val DIGITAL_STYLE = "digital_style" // TAMBAHAN

    fun face(context: Context): ClockFace {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(FACE, ClockFace.BLACK.name) ?: ClockFace.BLACK.name
        return runCatching { ClockFace.valueOf(value) }.getOrDefault(ClockFace.BLACK)
    }

    fun seconds(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(SECONDS, false)

    // TAMBAHAN: Fungsi untuk membaca dan menyimpan gaya jam digital
    fun digitalStyle(context: Context): DigitalStyle {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(DIGITAL_STYLE, DigitalStyle.NONE.name) ?: DigitalStyle.NONE.name
        return runCatching { DigitalStyle.valueOf(value) }.getOrDefault(DigitalStyle.NONE)
    }

    fun save(context: Context, face: ClockFace, seconds: Boolean, digitalStyle: DigitalStyle) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(FACE, face.name)
            .putBoolean(SECONDS, seconds)
            .putString(DIGITAL_STYLE, digitalStyle.name) // TAMBAHAN
            .apply()
    }
}
