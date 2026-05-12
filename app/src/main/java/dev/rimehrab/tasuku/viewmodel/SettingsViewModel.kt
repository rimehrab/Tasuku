package dev.rimehrab.tasuku.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private var _theme by mutableStateOf(prefs.getString("theme", "system") ?: "system")
    val theme: String get() = _theme

    private var _dynamicColor by mutableStateOf(prefs.getBoolean("dynamic_color", true))
    val dynamicColor: Boolean get() = _dynamicColor

    fun setTheme(newTheme: String) {
        _theme = newTheme
        prefs.edit().putString("theme", newTheme).apply()
    }

    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor = enabled
        prefs.edit().putBoolean("dynamic_color", enabled).apply()
    }
}
