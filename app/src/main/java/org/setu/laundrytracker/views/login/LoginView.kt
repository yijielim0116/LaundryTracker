package org.setu.laundrytracker.views.login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.setu.laundrytracker.R

// Login screen - shows after the splash when nobody is logged in
// the presenter checks the form and asks Firebase Auth
class LoginView : AppCompatActivity() {

    private lateinit var presenter: LoginPresenter

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

        presenter = LoginPresenter(this)

        emailInput =
            findViewById(R.id.emailInput)

        passwordInput =
            findViewById(R.id.passwordInput)

        loginButton =
            findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            presenter.doLogin(
                emailInput.text.toString().trim(),
                passwordInput.text.toString()
            )
        }

        findViewById<Button>(R.id.goToSignupButton).setOnClickListener {
            presenter.doGoToSignup()
        }
    }

    fun showEmailError(message: String) {
        emailInput.error = message
    }

    fun showPasswordError(message: String) {
        passwordInput.error = message
    }

    fun showBusy(busy: Boolean) {
        loginButton.isEnabled = !busy
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
