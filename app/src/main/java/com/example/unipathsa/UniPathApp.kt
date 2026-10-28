package com.example.unipathsa

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

class UniPathApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        val savedLanguage = prefs.getString("app_language", null)
        if (!savedLanguage.isNullOrBlank()) {
            val appLocales = LocaleListCompat.forLanguageTags(savedLanguage)
            AppCompatDelegate.setApplicationLocales(appLocales)
        }
    }
}