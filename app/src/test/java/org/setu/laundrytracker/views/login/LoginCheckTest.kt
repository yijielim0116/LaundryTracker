package org.setu.laundrytracker.views.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// the login form checks, before anything goes to Firebase
class LoginCheckTest {

    @Test
    fun emptyEmailIsBlocked() {
        assertEquals("Email is required", LoginPresenter.emailError(""))
    }

    @Test
    fun anyEmailIsOkForLogin() {
        // login doesn't check the format, Firebase just says wrong email or password
        assertNull(LoginPresenter.emailError("me@test.com"))
    }

    @Test
    fun emptyPasswordIsBlocked() {
        assertEquals("Password is required", LoginPresenter.passwordError(""))
    }

    @Test
    fun passwordIsOk() {
        assertNull(LoginPresenter.passwordError("abc"))
    }
}
