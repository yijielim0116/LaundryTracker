package org.setu.laundrytracker.main

import androidx.appcompat.app.AppCompatDelegate
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSettingTest {

    @Test
    fun eachChoiceTurnsIntoTheRightNightMode() {
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeSetting.toNightMode(ThemeSetting.SYSTEM))
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, ThemeSetting.toNightMode(ThemeSetting.LIGHT))
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, ThemeSetting.toNightMode(ThemeSetting.DARK))
    }

    @Test
    fun anUnknownChoiceFollowsThePhone() {
        // e.g. a bad number saved somehow, the app should still work
        assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, ThemeSetting.toNightMode(99))
    }
}
