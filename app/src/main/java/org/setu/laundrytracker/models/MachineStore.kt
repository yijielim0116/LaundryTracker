package org.setu.laundrytracker.models

interface MachineStore {
    fun findAll(): List<MachineModel>
    fun create(machine: MachineModel)
    fun update(machine: MachineModel): Boolean
    fun delete(id: Long): Boolean
    fun findOne(id: Long): MachineModel?

    // stores that load in the background (Firestore) call this when the machines change,
    // so the screen can redraw. MemStore / JSONStore have everything straight away so they don't need it
    fun setOnChangeListener(listener: (() -> Unit)?) {}
}
