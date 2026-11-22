package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.main.AppData
import timber.log.Timber

// Sign up screen - makes a new account and logs you straight in
class SignupActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

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

        confirmInput =
            findViewById(R.id.confirmInput)

        signupButton =
            findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            signUp()
        }

        // login opened this screen, so finishing just goes back to it
        findViewById<Button>(R.id.goToLoginButton).setOnClickListener {
            finish()
        }
    }

    private fun signUp() {
        val email =
            emailInput.text.toString().trim()

        val password =
            passwordInput.text.toString()

        val confirm =
            confirmInput.text.toString()

        // Patterns.EMAIL_ADDRESS is Android's own check for something@something.something
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Timber.d("Sign up blocked: email not valid")
            emailInput.error = "Enter a valid email"
            return
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            Timber.d("Sign up blocked: password too short")
            passwordInput.error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            return
        }

        if (password != confirm) {
            Timber.d("Sign up blocked: passwords don't match")
            confirmInput.error = "Passwords don't match"
            return
        }

        // Firebase makes the account on its server (and keeps the password safe, not me)
        // and logs you straight in when it works
        signupButton.isEnabled = false

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                signupButton.isEnabled = true

                if (task.isSuccessful) {
                    Timber.d("Account created and logged in: $email")

                    Toast.makeText(
                        this,
                        "Account created",
                        Toast.LENGTH_SHORT
                    ).show()

                    openHome()
                } else {
                    Timber.d("Sign up failed for $email: ${task.exception?.javaClass?.simpleName}")
                    showSignupError(task.exception)
                }
            }
    }

    // Firebase tells me why it failed, so the error goes on the right box
    private fun showSignupError(error: Exception?) {
        when (error) {
            is FirebaseAuthUserCollisionException ->
                emailInput.error = "An account with this email already exists"

            is FirebaseAuthWeakPasswordException ->
                passwordInput.error = "That password is too weak"

            is FirebaseNetworkException ->
                Toast.makeText(this, "No internet connection, try again", Toast.LENGTH_SHORT).show()

            else ->
                Toast.makeText(this, "Couldn't create the account, try again", Toast.LENGTH_SHORT).show()
        }
    }

    // CLEAR_TASK wipes login + sign up off the back stack, so Back on home doesn't come back here
    private fun openHome() {
        // load this user's machines from Firestore before Home opens
        Session.userId()?.let { AppData.useCloudStoreFor(it) }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
