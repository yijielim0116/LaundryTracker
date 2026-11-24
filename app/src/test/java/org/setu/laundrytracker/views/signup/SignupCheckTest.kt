package org.setu.laundrytracker.views.signup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

// the sign up form checks, before anything goes to Firebase
class SignupCheckTest {

    @Test
    fun goodEmailsPass() {
        assertNull(SignupPresenter.emailError("me@test.com"))
        assertNull(SignupPresenter.emailError("first.last+laundry@setu.ie"))
    }

    @Test
    fun badEmailsAreBlocked() {
        assertEquals("Enter a valid email", SignupPresenter.emailError(""))
        assertNotNull(SignupPresenter.emailError("me"))
        assertNotNull(SignupPresenter.emailError("me@test"))
        assertNotNull(SignupPresenter.emailError("@test.com"))
        assertNotNull(SignupPresenter.emailError("me @test.com"))
    }

    @Test
    fun shortPasswordIsBlocked() {
        assertEquals(
            "Password must be at least 6 characters",
            SignupPresenter.passwordError("12345")
        )
    }

    @Test
    fun sixCharacterPasswordIsOk() {
        assertNull(SignupPresenter.passwordError("123456"))
    }

    @Test
    fun differentConfirmIsBlocked() {
        assertEquals("Passwords don't match", SignupPresenter.confirmError("123456", "123457"))
    }

    @Test
    fun sameConfirmIsOk() {
        assertNull(SignupPresenter.confirmError("123456", "123456"))
    }
}
