package org.setu.laundrytracker.main

import org.setu.laundrytracker.models.MachineFirestoreStore
import org.setu.laundrytracker.models.MachineMemStore
import org.setu.laundrytracker.models.MachineStore
import timber.log.Timber

/**
 * Holds the machine store that every screen shares.
 */
// same idea as AppData in my PlacedMark lab
object AppData {

    // the screens only use the MachineStore functions so they don't care which store it is
    // private set = only these functions can change it, a screen can't swap the store by accident
    lateinit var machines: MachineStore
        private set

    // runs once when the app starts (MainApp)
    // Firebase remembers who is logged in, so if someone is, load their machines from Firestore straight away
    fun initialize() {
        if (::machines.isInitialized) return

        val userId = Session.userId()
        if (userId != null) {
            useCloudStoreFor(userId)
        } else {
            // nobody logged in yet, an empty store until they log in
            machines = MachineMemStore()
        }
    }

    // after logging in / signing up: each user gets their own machines in Firestore
    fun useCloudStoreFor(userId: String) {
        stopCloudStore()
        machines = MachineFirestoreStore(userId)
        Timber.d("Using Firestore machines for user $userId")
    }

    // on log out: stop listening to Firestore and forget the last user's machines
    fun signedOut() {
        stopCloudStore()
        machines = MachineMemStore()
    }

    private fun stopCloudStore() {
        if (::machines.isInitialized) {
            (machines as? MachineFirestoreStore)?.stop()
        }
    }
}
