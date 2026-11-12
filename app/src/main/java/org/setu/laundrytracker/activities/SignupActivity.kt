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
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.models.Passwords
import org.setu.laundrytracker.models.UserModel

// Sign up screen - makes a new account and logs you straight in
class SignupActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText

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

        findViewById<Button>(R.id.signupButton).setOnClickListener {
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
            emailInput.error = "Enter a valid email"
            return
        }

        if (AppData.users.findByEmail(email) != null) {
            emailInput.error = "An account with this email already exists"
            return
        }

        if (password.length < MIN_PASSWORD_LENGTH) {
            passwordInput.error = "Password must be at least $MIN_PASSWORD_LENGTH characters"
            return
        }

        if (password != confirm) {
            confirmInput.error = "Passwords don't match"
            return
        }

        // only the hash + salt get saved, never the password itself
        val salt = Passwords.newSalt()
        AppData.users.create(
            UserModel(
                email = email,
                passwordHash = Passwords.hash(password, salt),
                salt = salt
            )
        )

        Session.login(this, email)

        Toast.makeText(
            this,
            "Account created",
            Toast.LENGTH_SHORT
        ).show()

        openHome()
    }

    // CLEAR_TASK wipes login + sign up off the back stack, so Back on home doesn't come back here
    private fun openHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
