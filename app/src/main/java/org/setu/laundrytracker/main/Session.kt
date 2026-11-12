package org.setu.laundrytracker.main

import android.content.Context

/**
 * Remembers who is logged in, so you don't have to log in every time the app opens.
 */
// SharedPreferences is a small key-value file the app keeps, perfect for one email
object Session {

    private const val PREFS_NAME = "session"
    private const val KEY_EMAIL = "email"

    fun login(context: Context, email: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_EMAIL)
            .apply()
    }

    // null means nobody is logged in
    fun currentEmail(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EMAIL, null)
    }
}
