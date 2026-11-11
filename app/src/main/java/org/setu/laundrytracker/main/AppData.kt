package org.setu.laundrytracker.main

import org.setu.laundrytracker.models.MachineStore

/**
 * Holds the one machine store that every screen shares.
 */
object AppData {
    // set once by MainApp when the app starts (it's the JSON store),
    // the screens only use the MachineStore functions so they don't care which store it is
    lateinit var machines: MachineStore
}
