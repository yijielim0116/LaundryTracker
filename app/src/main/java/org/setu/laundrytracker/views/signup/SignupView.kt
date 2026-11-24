package org.setu.laundrytracker.views.signup

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.setu.laundrytracker.R

// Sign up screen - makes a new account and logs you straight in
// the presenter does the checks and the Firebase part
class SignupView : AppCompatActivity() {

    private lateinit var presenter: SignupPresenter

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        // stops the form going under the status bar / nav bar, and under the keyboard too
        // (edge to edge means the screen doesn't shrink for the keyboard by itself,
        // so without the ime part the keyboard covers the button)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        presenter = SignupPresenter(this)

        emailInput =
            findViewById(R.id.emailInput)

        passwordInput =
            findViewById(R.id.passwordInput)

        confirmInput =
            findViewById(R.id.confirmInput)

        signupButton =
            findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            presenter.doSignup(
                emailInput.text.toString().trim(),
                passwordInput.text.toString(),
                confirmInput.text.toString()
            )
        }

        // Done on the keyboard in the last box does the same as tapping the button
        confirmInput.setOnEditorActionListener { _, actionId, event ->
            // a real Enter key sends a press and a release, so only act on the press (or it logs in twice)
            val pressed = event == null || event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_DONE && pressed) {
                signupButton.performClick()
                true
            } else {
                false
            }
        }

        findViewById<Button>(R.id.goToLoginButton).setOnClickListener {
            presenter.doGoToLogin()
        }
    }

    fun showEmailError(message: String) {
        emailInput.error = message
    }

    fun showPasswordError(message: String) {
        passwordInput.error = message
    }

    fun showConfirmError(message: String) {
        confirmInput.error = message
    }

    fun showBusy(busy: Boolean) {
        signupButton.isEnabled = !busy
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
