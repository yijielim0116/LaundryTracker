package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.setu.laundrytracker.R

// Splash screen - shows my logo for a bit, then goes to the home screen
class SplashActivity : AppCompatActivity() {

    // I keep the handler so I can cancel it if the user leaves early
    private val handler = Handler(Looper.getMainLooper())

    // what to do when the time is up
    private val goToHome = Runnable {
        startActivity(Intent(this, MainActivity::class.java))
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
