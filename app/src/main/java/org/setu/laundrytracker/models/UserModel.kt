package org.setu.laundrytracker.models

// one account, I never keep the real password, only its hash + the salt used to make it
data class UserModel(
    var id: Long = 0L,
    val email: String,
    val passwordHash: String,
    val salt: String
)
