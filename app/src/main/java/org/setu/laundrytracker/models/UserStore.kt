package org.setu.laundrytracker.models

interface UserStore {
    fun findAll(): List<UserModel>
    fun create(user: UserModel)
    fun findByEmail(email: String): UserModel?
}
