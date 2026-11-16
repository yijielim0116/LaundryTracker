package org.setu.laundrytracker.main

import android.content.Context
import org.setu.laundrytracker.models.MachineJSONStore
import org.setu.laundrytracker.models.MachineStore
import org.setu.laundrytracker.models.UserJSONStore
import org.setu.laundrytracker.models.UserStore
import java.io.File

/**
 * Holds the stores that every screen shares.
 */
// same idea as AppData in my PlacedMark lab
object AppData {

    // the screens only use the MachineStore functions so they don't care which store it is
    // private set = only initialize() can set it, a screen can't swap the store by accident
    lateinit var machines: MachineStore
        private set

    // the accounts for sign up / login
    lateinit var users: UserStore
        private set

    // makes the stores once, using json files in the app's own files folder
    // filesDir is the app's private folder on the phone, so nothing else can mess with the files
    fun initialize(context: Context) {
        // already done, no need to load the files twice
        if (::machines.isInitialized) return

        machines = MachineJSONStore(File(context.filesDir, "machines.json"))
        users = UserJSONStore(File(context.filesDir, "users.json"))
    }
}
