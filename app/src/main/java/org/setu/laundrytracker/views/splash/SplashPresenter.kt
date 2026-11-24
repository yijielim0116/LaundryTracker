package org.setu.laundrytracker.views.splash

import android.content.Intent
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.views.home.HomeView
import org.setu.laundrytracker.views.login.LoginView
import timber.log.Timber

// decides where the app goes after the splash, the view just waits and tells me when the time is up
class SplashPresenter(private val view: SplashView) {

    fun doSplashDone() {
        // logged in already -> home screen, otherwise they have to log in first
        val next = if (Session.isLoggedIn()) {
            HomeView::class.java
        } else {
            LoginView::class.java
        }
        Timber.d("Splash done, opening ${next.simpleName}")
        view.startActivity(Intent(view, next))
        // finish so pressing back on the home screen doesn't bring the splash back
        view.finish()
    }
}
