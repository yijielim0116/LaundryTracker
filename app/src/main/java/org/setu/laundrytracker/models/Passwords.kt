package org.setu.laundrytracker.models

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Hashes passwords so I never save the real password in users.json.
 */
object Passwords {

    // a random salt for every user, so two people with the same password get different hashes
    fun newSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return toHex(bytes)
    }

    // SHA-256 of salt + password, written as hex text so it fits in the JSON
    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return toHex(digest.digest((salt + password).toByteArray()))
    }

    // hash what they typed with their salt and compare it to the saved hash
    fun matches(password: String, user: UserModel): Boolean {
        return hash(password, user.salt) == user.passwordHash
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
