package org.setu.laundrytracker.views.machinelist

import org.junit.Assert.assertEquals
import org.junit.Test
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType
import org.setu.laundrytracker.views.machinelist.MachineListPresenter.Companion.filterMachines

class MachineListFilterTest {

    private val machines = listOf(
        MachineModel(id = 1, name = "Washer 1", location = "Block A", status = MachineStatus.AVAILABLE),
        MachineModel(id = 2, name = "Washer 2", location = "Block B", status = MachineStatus.IN_USE),
        MachineModel(id = 3, name = "Dryer 1", location = "Block A", type = MachineType.DRYER, status = MachineStatus.OUT_OF_ORDER)
    )

    private fun ids(list: List<MachineModel>) = list.map { it.id }

    @Test
    fun emptySearchAndAllShowsEverything() {
        assertEquals(listOf(1L, 2L, 3L), ids(filterMachines(machines, "", null)))
    }

    @Test
    fun searchLooksAtNameAndLocationIgnoringCapitals() {
        assertEquals(listOf(1L, 2L), ids(filterMachines(machines, "WASHER", null)))
        assertEquals(listOf(1L, 3L), ids(filterMachines(machines, "block a", null)))
    }

    @Test
    fun statusFilterOnlyKeepsThatStatus() {
        assertEquals(listOf(2L), ids(filterMachines(machines, "", MachineStatus.IN_USE)))
    }

    @Test
    fun searchAndStatusBothHaveToMatch() {
        assertEquals(listOf(3L), ids(filterMachines(machines, "block a", MachineStatus.OUT_OF_ORDER)))
        assertEquals(emptyList<Long>(), ids(filterMachines(machines, "washer", MachineStatus.OUT_OF_ORDER)))
    }

    @Test
    fun noMatchGivesAnEmptyList() {
        assertEquals(emptyList<Long>(), ids(filterMachines(machines, "zzz", null)))
    }
}
