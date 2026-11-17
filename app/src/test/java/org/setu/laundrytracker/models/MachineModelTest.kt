package org.setu.laundrytracker.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MachineModelTest {

    @Test
    fun hasLocationOnlyWhenBothNumbersAreSet() {
        assertTrue(MachineModel(name = "Washer 1", latitude = 52.24, longitude = -7.13).hasLocation())

        // these ones can't go on the map
        assertFalse(MachineModel(name = "Washer 2").hasLocation())
        assertFalse(MachineModel(name = "Washer 3", latitude = 52.24).hasLocation())
        assertFalse(MachineModel(name = "Washer 4", longitude = -7.13).hasLocation())
    }

    @Test
    fun typeLabelsAreNormalWords() {
        assertEquals("Washer", MachineType.WASHER.label())
        assertEquals("Dryer", MachineType.DRYER.label())
    }

    @Test
    fun statusLabelsAreNormalWords() {
        assertEquals("Available", MachineStatus.AVAILABLE.label())
        assertEquals("In use", MachineStatus.IN_USE.label())
        assertEquals("Out of order", MachineStatus.OUT_OF_ORDER.label())
    }
}
