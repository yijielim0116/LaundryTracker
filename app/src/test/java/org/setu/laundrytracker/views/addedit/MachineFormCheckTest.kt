package org.setu.laundrytracker.views.addedit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.setu.laundrytracker.views.addedit.AddEditPresenter.Companion.locationError
import org.setu.laundrytracker.views.addedit.AddEditPresenter.Companion.nameError

// the add/edit form checks, before anything gets saved
class MachineFormCheckTest {

    private val others = listOf("Washer 1", "Dryer 2")

    @Test
    fun emptyNameIsBlocked() {
        assertEquals("Name is required", nameError("", others))
        assertEquals("Name is required", nameError("   ", others))
    }

    @Test
    fun newNameIsOk() {
        assertNull(nameError("Washer 3", others))
    }

    @Test
    fun takenNameIsBlocked() {
        assertEquals("A machine called Washer 1 already exists", nameError("Washer 1", others))
    }

    @Test
    fun takenNameIsBlockedWhateverTheCapitals() {
        assertEquals("A machine called washer 1 already exists", nameError(" washer 1 ", others))
    }

    @Test
    fun nameAtTheLimitIsOk() {
        assertNull(nameError("a".repeat(AddEditPresenter.MAX_NAME_LENGTH), others))
    }

    @Test
    fun nameOverTheLimitIsBlocked() {
        assertEquals(
            "Name can't be longer than 30 characters",
            nameError("a".repeat(AddEditPresenter.MAX_NAME_LENGTH + 1), others)
        )
    }

    @Test
    fun emptyLocationIsBlocked() {
        assertEquals("Location is required", locationError(""))
        assertEquals("Location is required", locationError("  "))
    }

    @Test
    fun locationIsOk() {
        assertNull(locationError("Block A, Floor 1"))
    }

    @Test
    fun locationOverTheLimitIsBlocked() {
        assertEquals(
            "Location can't be longer than 40 characters",
            locationError("a".repeat(AddEditPresenter.MAX_LOCATION_LENGTH + 1))
        )
    }
}
