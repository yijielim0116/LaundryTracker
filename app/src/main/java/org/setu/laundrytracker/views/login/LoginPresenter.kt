package org.setu.laundrytracker.views.login

import android.content.Intent
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.views.home.HomeView
import org.setu.laundrytracker.views.signup.SignupView
import timber.log.Timber

// checks the login form and talks to Firebase Auth, the view only shows what I tell it
class LoginPresenter(private val view: LoginView) {

    fun doLogin(email: String, password: String) {
        val emailProblem = emailError(email)
        if (emailProblem != null) {
            Timber.d("Login blocked: $emailProblem")
            view.showEmailError(emailProblem)
            return
        }

        val passwordProblem = passwordError(password)
        if (passwordProblem != null) {
            Timber.d("Login blocked: $passwordProblem")
            view.showPasswordError(passwordProblem)
            return
        }

        // Firebase checks the password on its server, so this takes a moment
        // greyed out until it answers so you can't tap Log in twice
        view.showBusy(true)

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(view) { task ->
                view.showBusy(false)

                if (task.isSuccessful) {
                    Timber.d("Login ok for $email")
                    openHome()
                } else {
                    // never log the password, just that it failed
                    Timber.d("Login failed for $email: ${task.exception?.javaClass?.simpleName}")
                    view.showMessage(loginErrorMessage(task.exception))
                }
            }
    }

    fun doGoToSignup() {
        Timber.d("Sign up link pressed")
        view.startActivity(Intent(view, SignupView::class.java))
    }

    // CLEAR_TASK so Back on the home screen closes the app instead of coming back to login
    private fun openHome() {
        // load this user's machines from Firestore before Home opens
        Session.userId()?.let { AppData.useCloudStoreFor(it) }

        val intent = Intent(view, HomeView::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        view.startActivity(intent)
    }

    // same message for a wrong email or a wrong password, so nobody can guess which emails have accounts
    private fun loginErrorMessage(error: Exception?) = when (error) {
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Wrong email or password"
        is FirebaseNetworkException -> "No internet connection, try again"
        else -> "Couldn't log in, try again"
    }

    // plain functions with no Android stuff, so my unit tests can check them
    // null means it's fine
    companion object {
        fun emailError(email: String): String? =
            if (email.isEmpty()) "Email is required" else null

        fun passwordError(password: String): String? =
            if (password.isEmpty()) "Password is required" else null
    }
}
