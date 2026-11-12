package org.setu.laundrytracker.main

import org.setu.laundrytracker.models.MachineStore
import org.setu.laundrytracker.models.UserStore

/**
 * Holds the stores that every screen shares.
 */
object AppData {
    // set once by MainApp when the app starts (it's the JSON store),
    // the screens only use the MachineStore functions so they don't care which store it is
    lateinit var machines: MachineStore

    // the accounts for sign up / login, also set by MainApp
    lateinit var users: UserStore
}
