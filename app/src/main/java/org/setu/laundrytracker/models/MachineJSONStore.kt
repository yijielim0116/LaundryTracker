package org.setu.laundrytracker.models

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import timber.log.Timber

/**
 * Keeps machines in a JSON file so they're still there after the app is closed.
 */
// same 5 functions as MachineMemStore (it's the same MachineStore contract),
// the difference is every change gets written to the file
class MachineJSONStore(private val file: File) : MachineStore {

    // pretty printing so the json file is easy to read if I open it (same as my PlacedMark lab)
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    // tells Gson the JSON is a list of MachineModel (not just one machine)
    private val listType = object : TypeToken<ArrayList<MachineModel>>() {}.type

    private var machines = ArrayList<MachineModel>()
    private val lastId = AtomicLong(0L)

    // read the file straight away so the machines from last time are back
    init {
        load()
    }

    override fun findAll(): List<MachineModel> {
        return machines
    }

    override fun create(machine: MachineModel) {
        machine.id = lastId.incrementAndGet()
        machines.add(machine)
        save()
        Timber.d("create: id=${machine.id}, name=${machine.name}")
    }

    override fun update(machine: MachineModel): Boolean {
        val foundIndex = machines.indexOfFirst { it.id == machine.id }
        return if (foundIndex != -1) {
            machines[foundIndex] = machines[foundIndex].copy(
                name = machine.name,
                location = machine.location,
                type = machine.type,
                status = machine.status,
                cycleMinutes = machine.cycleMinutes,
                imageUri = machine.imageUri,
                latitude = machine.latitude,
                longitude = machine.longitude
            )
            save()
            Timber.d("update: id=${machine.id}, name=${machine.name}")
            true
        } else {
            Timber.w("update: no machine with id=${machine.id}")
            false
        }
    }

    override fun delete(id: Long): Boolean {
        val foundMachine = findOne(id)
        return if (foundMachine != null) {
            machines.remove(foundMachine)
            save()
            Timber.d("delete: id=$id")
            true
        } else {
            Timber.w("delete: no machine with id=$id")
            false
        }
    }

    override fun findOne(id: Long): MachineModel? {
        return machines.find { m -> m.id == id }
    }

    // turns the whole list into JSON text and writes it over the file
    private fun save() {
        // make sure the folder exists first, like the lab does
        file.parentFile?.mkdirs()
        file.writeText(gson.toJson(machines, listType))
        Timber.d("save: ${machines.size} machines to ${file.name}")
    }

    // reads the file back into the list (first time the app runs there's no file yet)
    private fun load() {
        if (!file.exists()) {
            Timber.d("load: no ${file.name} yet, starting empty")
            return
        }

        machines = try {
            gson.fromJson<ArrayList<MachineModel>>(file.readText(), listType) ?: ArrayList()
        } catch (e: JsonParseException) {
            // the file is broken somehow, start with an empty list instead of crashing
            Timber.e(e, "load: ${file.name} is broken, starting empty")
            ArrayList()
        }

        // carry on the ids from the biggest one saved, so a new machine never gets an old id
        lastId.set(machines.maxOfOrNull { it.id } ?: 0L)
        Timber.d("load: ${machines.size} machines from ${file.name}")
    }
}
