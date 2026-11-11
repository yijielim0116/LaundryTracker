package org.setu.laundrytracker.models

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * Keeps machines in a JSON file so they're still there after the app is closed.
 */
// same 5 functions as MachineMemStore (it's the same MachineStore contract),
// the difference is every change gets written to the file
class MachineJSONStore(private val file: File) : MachineStore {

    private val gson = Gson()

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
    }

    override fun update(machine: MachineModel): Boolean {
        val foundIndex = machines.indexOfFirst { it.id == machine.id }
        return if (foundIndex != -1) {
            machines[foundIndex] = machines[foundIndex].copy(
                name = machine.name,
                location = machine.location,
                type = machine.type,
                status = machine.status,
                cycleMinutes = machine.cycleMinutes
            )
            save()
            true
        } else {
            false
        }
    }

    override fun delete(id: Long): Boolean {
        val foundMachine = findOne(id)
        return if (foundMachine != null) {
            machines.remove(foundMachine)
            save()
            true
        } else {
            false
        }
    }

    override fun findOne(id: Long): MachineModel? {
        return machines.find { m -> m.id == id }
    }

    // turns the whole list into JSON text and writes it over the file
    private fun save() {
        file.writeText(gson.toJson(machines, listType))
    }

    // reads the file back into the list (first time the app runs there's no file yet)
    private fun load() {
        if (!file.exists()) {
            return
        }

        machines = try {
            gson.fromJson<ArrayList<MachineModel>>(file.readText(), listType) ?: ArrayList()
        } catch (e: JsonParseException) {
            // the file is broken somehow, start with an empty list instead of crashing
            ArrayList()
        }

        // carry on the ids from the biggest one saved, so a new machine never gets an old id
        lastId.set(machines.maxOfOrNull { it.id } ?: 0L)
    }
}
