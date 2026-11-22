package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.main.AppData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import timber.log.Timber

// Login screen - shows after the splash when nobody is logged in
// the email + password are checked by Firebase Auth
class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // stops the form going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        emailInput =
            findViewById(R.id.emailInput)

        passwordInput =
            findViewById(R.id.passwordInput)

        loginButton =
            findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            logIn()
        }

        findViewById<Button>(R.id.goToSignupButton).setOnClickListener {
            Timber.d("Sign up link pressed")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun logIn() {
        val email =
            emailInput.text.toString().trim()

        val password =
            passwordInput.text.toString()

        if (email.isEmpty()) {
            Timber.d("Login blocked: email is empty")
            emailInput.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            Timber.d("Login blocked: password is empty")
            passwordInput.error = "Password is required"
            return
        }

        // Firebase checks the password on its server, so this takes a moment
        // greyed out until it answers so you can't tap Log in twice
        loginButton.isEnabled = false

        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                loginButton.isEnabled = true

                if (task.isSuccessful) {
                    Timber.d("Login ok for $email")
                    openHome()
                } else {
                    // never log the password, just that it failed
                    Timber.d("Login failed for $email: ${task.exception?.javaClass?.simpleName}")
                    showLoginError(task.exception)
                }
            }
    }

    // same message for a wrong email or a wrong password, so nobody can guess which emails have accounts
    private fun showLoginError(error: Exception?) {
        val message = when (error) {
            is FirebaseAuthInvalidUserException,
            is FirebaseAuthInvalidCredentialsException -> "Wrong email or password"
            is FirebaseNetworkException -> "No internet connection, try again"
            else -> "Couldn't log in, try again"
        }

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // CLEAR_TASK so Back on the home screen closes the app instead of coming back to login
    private fun openHome() {
        // load this user's machines from Firestore before Home opens
        Session.userId()?.let { AppData.useCloudStoreFor(it) }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
