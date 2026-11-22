package org.setu.laundrytracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MachineFirestoreMapperTest {

    @Test
    fun machineComesBackTheSameAfterGoingToFirestoreAndBack() {
        val machine = MachineModel(
            id = 7L,
            name = "Dryer 2",
            location = "Block C",
            type = MachineType.DRYER,
            status = MachineStatus.IN_USE,
            cycleMinutes = 45,
            imageUri = "content://test/photo.jpg",
            latitude = 52.245954,
            longitude = -7.139539
        )

        assertEquals(machine, machineFromFirestore(machine.toFirestoreMap()))
    }

    @Test
    fun enumsAreSavedAsTheirNames() {
        val map = MachineModel(name = "Washer 1", type = MachineType.WASHER, status = MachineStatus.OUT_OF_ORDER).toFirestoreMap()

        assertEquals("WASHER", map["type"])
        assertEquals("OUT_OF_ORDER", map["status"])
    }

    @Test
    fun firestoreNumberTypesAreConverted() {
        // Firestore hands whole numbers back as Long, even for Int fields
        val machine = machineFromFirestore(mapOf("id" to 3L, "name" to "Washer 3", "cycleMinutes" to 30L, "latitude" to 52L, "longitude" to -7.1))

        assertEquals(3L, machine.id)
        assertEquals(30, machine.cycleMinutes)
        assertEquals(52.0, machine.latitude!!, 0.0)
    }

    @Test
    fun missingFieldsFallBackToTheDefaults() {
        val machine = machineFromFirestore(mapOf("id" to 1L, "name" to "Washer 1"))

        assertEquals("", machine.location)
        assertEquals(MachineType.WASHER, machine.type)
        assertEquals(MachineStatus.AVAILABLE, machine.status)
        assertEquals(60, machine.cycleMinutes)
        assertNull(machine.imageUri)
        assertNull(machine.latitude)
    }

    @Test
    fun unknownStatusDoesNotCrash() {
        val machine = machineFromFirestore(mapOf("id" to 1L, "name" to "Washer 1", "status" to "EXPLODED"))

        assertEquals(MachineStatus.AVAILABLE, machine.status)
    }
}
