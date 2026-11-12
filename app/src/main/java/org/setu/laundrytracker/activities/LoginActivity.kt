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
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.models.Passwords

// Login screen - shows after the splash when nobody is logged in
class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

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

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            logIn()
        }

        findViewById<Button>(R.id.goToSignupButton).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun logIn() {
        val email =
            emailInput.text.toString().trim()

        val password =
            passwordInput.text.toString()

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return
        }

        val user = AppData.users.findByEmail(email)

        // same message for a wrong email or a wrong password, so nobody can guess which emails have accounts
        if (user == null || !Passwords.matches(password, user)) {
            Toast.makeText(
                this,
                "Wrong email or password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Session.login(this, user.email)

        // CLEAR_TASK so Back on the home screen closes the app instead of coming back to login
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
