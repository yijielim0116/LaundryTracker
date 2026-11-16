package org.setu.laundrytracker.models

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * Keeps the user accounts in users.json, same idea as MachineJSONStore.
 */
class UserJSONStore(private val file: File) : UserStore {

    // pretty printing so the json file is easy to read if I open it (same as my PlacedMark lab)
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // tells Gson the JSON is a list of UserModel
    private val listType = object : TypeToken<ArrayList<UserModel>>() {}.type

    private var users = ArrayList<UserModel>()
    private val lastId = AtomicLong(0L)

    init {
        load()
    }

    override fun findAll(): List<UserModel> {
        return users
    }

    override fun create(user: UserModel) {
        user.id = lastId.incrementAndGet()
        users.add(user)
        save()
    }

    // emails are compared without caring about capitals, so Test@x.com and test@x.com are the same account
    override fun findByEmail(email: String): UserModel? {
        return users.find { it.email.equals(email.trim(), ignoreCase = true) }
    }

    private fun save() {
        // make sure the folder exists first, like the lab does
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(users, listType))
    }

    private fun load() {
        if (!file.exists()) {
            return
        }

        users = try {
            gson.fromJson<ArrayList<UserModel>>(file.readText(), listType) ?: ArrayList()
        } catch (e: JsonParseException) {
            // broken file, start empty instead of crashing
            ArrayList()
        }

        lastId.set(users.maxOfOrNull { it.id } ?: 0L)
    }
}
