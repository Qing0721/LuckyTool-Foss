@file:Suppress("unused")

package com.fosstool.app.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

object ThemeUtils {

    private val supportDynamicColor = DynamicColors.isDynamicColorAvailable()

    fun isDynamicColor(context: Context): Boolean {
        val useDynamicColor = context.getBoolean(SettingsPrefs, "use_dynamic_color", false)
        return supportDynamicColor && useDynamicColor
    }

    fun isFollowSystem(context: Context): Boolean {
        val followSystem = context.getBoolean(SettingsPrefs, "theme_follow_system", true)
        return supportDynamicColor && followSystem
    }

    val Context.isNightMode get() = isNightMode(resources.configuration)

    fun isNightMode(configuration: Configuration): Boolean {
        return (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    fun initTheme(string: String?) {
        when (string) {
            "MODE_NIGHT_FOLLOW_SYSTEM" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "MODE_NIGHT_NO" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "MODE_NIGHT_YES" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
