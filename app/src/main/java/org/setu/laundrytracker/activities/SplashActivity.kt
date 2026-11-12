package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.Session

// Splash screen - shows my logo for a bit, then goes to login (or home if already logged in)
class SplashActivity : AppCompatActivity() {

    // I keep the handler so I can cancel it if the user leaves early
    private val handler = Handler(Looper.getMainLooper())

    // what to do when the time is up
    private val goToHome = Runnable {
        // logged in already -> home screen, otherwise they have to log in first
        val next = if (Session.currentEmail(this) != null) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }
        startActivity(Intent(this, next))
        // finish so pressing back on the home screen doesn't bring the splash back
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // wait 2 seconds then open the home screen
        handler.postDelayed(goToHome, SPLASH_TIME_MS)
    }

    override fun onDestroy() {
        // if they press back during the splash, don't open home after they left
        handler.removeCallbacks(goToHome)
        super.onDestroy()
    }

    companion object {
        const val SPLASH_TIME_MS = 2000L
    }
}
