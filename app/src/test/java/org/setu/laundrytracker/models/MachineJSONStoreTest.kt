package org.setu.laundrytracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MachineJSONStoreTest {

    // JUnit makes a fresh temp folder for every test and deletes it after
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var file: File

    @Before
    fun setUp() {
        file = File(tempFolder.root, "machines.json")
    }

    // a new store reading the same file = what happens when the app is opened again
    private fun reopen(): MachineJSONStore {
        return MachineJSONStore(file)
    }

    @Test
    fun startsEmptyWhenThereIsNoFile() {
        assertEquals(0, reopen().findAll().size)
    }

    @Test
    fun createdMachinesAreStillThereAfterReopening() {
        val store = reopen()
        store.create(MachineModel(name = "Washer 1", location = "Block A"))
        store.create(MachineModel(name = "Dryer 1", type = MachineType.DRYER, status = MachineStatus.IN_USE, cycleMinutes = 45))

        val all = reopen().findAll()
        assertEquals(2, all.size)
        assertEquals("Washer 1", all[0].name)
        assertEquals("Block A", all[0].location)
        assertEquals(MachineType.DRYER, all[1].type)
        assertEquals(MachineStatus.IN_USE, all[1].status)
        assertEquals(45, all[1].cycleMinutes)
    }

    @Test
    fun updateIsSavedToTheFile() {
        val store = reopen()
        store.create(MachineModel(name = "Washer 1"))
        assertTrue(store.update(MachineModel(id = 1L, name = "Washer A", status = MachineStatus.OUT_OF_ORDER)))

        val machine = reopen().findOne(1L)
        assertEquals("Washer A", machine?.name)
        assertEquals(MachineStatus.OUT_OF_ORDER, machine?.status)
    }

    @Test
    fun deleteIsSavedToTheFile() {
        val store = reopen()
        store.create(MachineModel(name = "Washer 1"))
        store.create(MachineModel(name = "Washer 2"))
        assertTrue(store.delete(1L))

        val reopened = reopen()
        assertNull(reopened.findOne(1L))
        assertEquals(1, reopened.findAll().size)
        assertFalse(reopened.delete(1L))
    }

    // ids carry on from the biggest saved id, so an old id is never reused
    @Test
    fun idsCarryOnAfterReopening() {
        val store = reopen()
        store.create(MachineModel(name = "Washer 1"))
        store.create(MachineModel(name = "Washer 2"))
        store.delete(2L)

        val reopened = reopen()
        reopened.create(MachineModel(name = "Washer 3"))
        assertEquals(2L, reopened.findAll().last().id)

        reopened.create(MachineModel(name = "Washer 4"))
        assertEquals(3L, reopened.findAll().last().id)
    }

    // a broken file shouldn't crash the app, it just starts empty
    @Test
    fun brokenFileStartsEmpty() {
        file.writeText("this is not json")
        assertEquals(0, reopen().findAll().size)
    }
}
