package org.setu.laundrytracker.views.signup

import android.content.Intent
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.views.home.HomeView
import timber.log.Timber

// checks the sign up form and makes the account with Firebase Auth
class SignupPresenter(private val view: SignupView) {

    fun doSignup(email: String, password: String, confirm: String) {
        val emailProblem = emailError(email)
        if (emailProblem != null) {
            Timber.d("Sign up blocked: $emailProblem")
            view.showEmailError(emailProblem)
            return
        }

        val passwordProblem = passwordError(password)
        if (passwordProblem != null) {
            Timber.d("Sign up blocked: $passwordProblem")
            view.showPasswordError(passwordProblem)
            return
        }

        val confirmProblem = confirmError(password, confirm)
        if (confirmProblem != null) {
            Timber.d("Sign up blocked: $confirmProblem")
            view.showConfirmError(confirmProblem)
            return
        }

        // Firebase makes the account on its server (and keeps the password safe, not me)
        // and logs you straight in when it works
        view.showBusy(true)

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(view) { task ->
                view.showBusy(false)

                if (task.isSuccessful) {
                    Timber.d("Account created and logged in: $email")
                    view.showMessage("Account created")
                    openHome()
                } else {
                    Timber.d("Sign up failed for $email: ${task.exception?.javaClass?.simpleName}")
                    showSignupError(task.exception)
                }
            }
    }

    // login opened this screen, so finishing just goes back to it
    fun doGoToLogin() {
        view.finish()
    }

    // Firebase tells me why it failed, so the error goes on the right box
    private fun showSignupError(error: Exception?) {
        when (error) {
            is FirebaseAuthUserCollisionException ->
                view.showEmailError("An account with this email already exists")

            is FirebaseAuthWeakPasswordException ->
                view.showPasswordError("That password is too weak")

            is FirebaseNetworkException ->
                view.showMessage("No internet connection, try again")

            else ->
                view.showMessage("Couldn't create the account, try again")
        }
    }

    // CLEAR_TASK wipes login + sign up off the back stack, so Back on home doesn't come back here
    private fun openHome() {
        // load this user's machines from Firestore before Home opens
        Session.userId()?.let { AppData.useCloudStoreFor(it) }

        val intent = Intent(view, HomeView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        view.startActivity(intent)
    }

    // plain functions with no Android stuff, so my unit tests can check them
    // null means it's fine
    companion object {
        const val MIN_PASSWORD_LENGTH = 6

        // same pattern as Android's Patterns.EMAIL_ADDRESS (something@something.something)
        // copied as a Kotlin Regex because Patterns doesn't work in plain unit tests
        private val EMAIL_PATTERN = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
        )

        fun emailError(email: String): String? =
            if (!EMAIL_PATTERN.matches(email)) "Enter a valid email" else null

        fun passwordError(password: String): String? =
            if (password.length < MIN_PASSWORD_LENGTH) {
                "Password must be at least $MIN_PASSWORD_LENGTH characters"
            } else {
                null
            }

        fun confirmError(password: String, confirm: String): String? =
            if (password != confirm) "Passwords don't match" else null
    }
}
