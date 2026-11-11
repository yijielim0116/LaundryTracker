package org.setu.laundrytracker.main

import android.app.Application
import org.setu.laundrytracker.models.MachineJSONStore
import java.io.File

// runs once when the app starts, before any screen opens (registered in the manifest)
class MainApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // filesDir is the app's own private folder on the phone,
        // so machines.json lives there and nothing else can mess with it
        AppData.machines = MachineJSONStore(File(filesDir, "machines.json"))
    }
}
