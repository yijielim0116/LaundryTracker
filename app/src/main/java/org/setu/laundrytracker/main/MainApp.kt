package org.setu.laundrytracker.main

import android.app.Application
import timber.log.Timber

// runs once when the app starts, before any screen opens (registered in the manifest)
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // plant Timber once for the whole app so every Timber.d() shows up in Logcat
        // (the lab does this in MainActivity, MainApp runs before every screen so it's here)
        // the check stops a second tree being added, which would double every log
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("MainApp created")

        // put the app in light / dark / follow-the-phone mode before the first screen shows
        ThemeSetting.applySaved(this)

        // the lab calls this in MainActivity, but my app opens Splash and Login before Home,
        // so I load the store here instead.
        // MainApp always runs first, even when Android reopens the app straight on another screen
        AppData.initialize(this)
    }
}
