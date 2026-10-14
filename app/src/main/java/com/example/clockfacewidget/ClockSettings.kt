package com.example.clockfacewidget

import android.content.Context

// Pilihan Tipe Jam
enum class ClockType(val label: String) {
    ANALOG("Jam Analog"),
    DIGITAL("Jam Digital")
}

// Pilihan Tema Jam Analog
enum class AnalogTheme(val label: String) {
    BLACK("Classic Black"),
    WHITE("Classic White"),
    SPACE("Space"),
    ROMAN("Roman")
}

// Pilihan Tema Jam Digital
enum class DigitalTheme(val label: String) {
    BIG_WHITE("Digital Besar (Putih)"),
    BIG_BLACK("Digital Besar (Hitam)")
}

object ClockSettings {
    private const val PREF = "clock_settings"
    private const val KEY_TYPE = "clock_type"
    private const val KEY_ANALOG_THEME = "analog_theme"
    private const val KEY_DIGITAL_THEME = "digital_theme"
    private const val KEY_SECONDS = "seconds"

    fun type(context: Context): ClockType {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_TYPE, ClockType.ANALOG.name) ?: ClockType.ANALOG.name
        return runCatching { ClockType.valueOf(value) }.getOrDefault(ClockType.ANALOG)
    }

    fun analogTheme(context: Context): AnalogTheme {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ANALOG_THEME, AnalogTheme.BLACK.name) ?: AnalogTheme.BLACK.name
        return runCatching { AnalogTheme.valueOf(value) }.getOrDefault(AnalogTheme.BLACK)
    }

    fun digitalTheme(context: Context): DigitalTheme {
        val value = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_DIGITAL_THEME, DigitalTheme.BIG_WHITE.name) ?: DigitalTheme.BIG_WHITE.name
        return runCatching { DigitalTheme.valueOf(value) }.getOrDefault(DigitalTheme.BIG_WHITE)
    }

    fun seconds(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_SECONDS, false)

    fun save(
        context: Context,
        type: ClockType,
        analogTheme: AnalogTheme,
        digitalTheme: DigitalTheme,
        seconds: Boolean
    ) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TYPE, type.name)
            .putString(KEY_ANALOG_THEME, analogTheme.name)
            .putString(KEY_DIGITAL_THEME, digitalTheme.name)
            .putBoolean(KEY_SECONDS, seconds)
            .apply()
    }
}
