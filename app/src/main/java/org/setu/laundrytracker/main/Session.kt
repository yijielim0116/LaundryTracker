package org.setu.laundrytracker.main

import com.google.firebase.auth.FirebaseAuth

/**
 * Who is logged in, now worked out by Firebase Auth.
 */
// Firebase remembers the logged-in user by itself (even after the app is closed),
// so I don't need my own SharedPreferences for this anymore
object Session {

    // Firebase's id for the user, each user's machines are saved under it in Firestore
    fun userId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // null means nobody is logged in
    fun currentEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }
}
