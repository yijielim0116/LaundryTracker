package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.Session

// Home screen - the first thing you see when the app opens
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewMachinesButton =
            findViewById<Button>(R.id.viewMachinesButton)

        // go to the list of all the machines
        viewMachinesButton.setOnClickListener {
            startActivity(
                Intent(this, MachineListActivity::class.java)
            )
        }

        findViewById<TextView>(R.id.loggedInText).text =
            "Logged in as ${Session.currentEmail(this)}"

        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logOut()
        }
    }

    private fun logOut() {
        Session.logout(this)

        // CLEAR_TASK so Back on the login screen can't get back into the app
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
