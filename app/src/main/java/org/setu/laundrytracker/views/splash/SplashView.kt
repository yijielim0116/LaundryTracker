package org.setu.laundrytracker.views.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.setu.laundrytracker.R

// Splash screen - shows my logo for a bit, then the presenter picks login or home
class SplashView : AppCompatActivity() {

    private lateinit var presenter: SplashPresenter

    // I keep the handler so I can cancel it if the user leaves early
    private val handler = Handler(Looper.getMainLooper())

    // what to do when the time is up
    private val splashDone = Runnable { presenter.doSplashDone() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        presenter = SplashPresenter(this)

        // wait 2 seconds then move on
        handler.postDelayed(splashDone, SPLASH_TIME_MS)
    }

    override fun onDestroy() {
        // if they press back during the splash, don't open home after they left
        handler.removeCallbacks(splashDone)
        super.onDestroy()
    }

    companion object {
        const val SPLASH_TIME_MS = 2000L
    }
}
