package org.setu.laundrytracker.main

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Remembers if the app should follow the phone, or always be light / always be dark.
 */
// saved in SharedPreferences like Session, so the choice is still there next time the app opens
object ThemeSetting {

    // the 3 choices, in the same order as the options in the Dark mode dialog
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2

    private const val PREFS_NAME = "settings"
    private const val KEY_THEME = "theme"

    fun saved(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_THEME, SYSTEM)
    }

    fun save(context: Context, choice: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME, choice)
            .apply()
    }

    // turns my choice into AppCompat's night mode setting
    fun toNightMode(choice: Int): Int {
        return when (choice) {
            LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    }

    // AppCompat redraws every open screen in the new mode by itself
    fun apply(choice: Int) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(choice))
    }

    fun applySaved(context: Context) {
        apply(saved(context))
    }
}
