package org.setu.laundrytracker.main

import android.app.Application

// runs once when the app starts, before any screen opens (registered in the manifest)
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // the lab calls this in MainActivity, but my app opens Splash and Login before Home
        // (and Login already needs AppData.users), so I load the stores here instead.
        // MainApp always runs first, even when Android reopens the app straight on another screen
        AppData.initialize(this)
    }
}
