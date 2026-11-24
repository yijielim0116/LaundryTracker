package org.setu.laundrytracker.views.machinelist

import android.content.Intent
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.views.addedit.AddEditView
import timber.log.Timber

// decides which machines show and what happens when I add, edit or delete one
// the view only shows the list, the empty message and the search / filter controls
// (same idea as MarkListPresenter in my PlacedMark lab)
class MachineListPresenter(private val view: MachineListView) {

    // what's typed in the search box and picked in the drop-down, null status means "All"
    private var search = ""
    private var status: MachineStatus? = null

    // the view calls this when it comes back, so an add / edit / delete shows up
    fun doResume() {
        showMachines()

        // Firestore loads in the background, so show the list again whenever the machines change
        AppData.machines.setOnChangeListener { showMachines() }
    }

    // stop redrawing a screen I can't see
    fun doPause() {
        AppData.machines.setOnChangeListener(null)
    }

    fun doSearchChanged(text: String) {
        search = text.trim()
        showMachines()
    }

    fun doStatusFilterChanged(newStatus: MachineStatus?) {
        status = newStatus
        showMachines()
    }

    // the view empties the search box and sets the drop-down back to All,
    // and those changes come back here through doSearchChanged / doStatusFilterChanged
    fun doClearFilters() {
        Timber.d("Clear filters pressed")
        view.clearFilterControls()
    }

    // the round + button opens the form to add a new machine
    fun doAddMachine() {
        view.startActivity(Intent(view, AddEditView::class.java))
    }

    // Edit pressed on a row: open the form and send the machine's id so it knows which one to edit
    fun doEditMachine(machine: MachineModel) {
        Timber.d("Edit pressed, machine id=${machine.id}")

        val intent = Intent(view, AddEditView::class.java)
        intent.putExtra("id", machine.id)
        view.startActivity(intent)
    }

    // Delete pressed on a row: remove it from the store then show the list again
    fun doDeleteMachine(machine: MachineModel) {
        Timber.d("Delete pressed, machine id=${machine.id}")

        AppData.machines.delete(machine.id)
        showMachines()
    }

    // filters the machines and tells the view what to show
    private fun showMachines() {
        val allMachines = AppData.machines.findAll()
        val machines = filterMachines(allMachines, search, status)

        view.showMachines(machines)
        Timber.d("Showing ${machines.size} of ${allMachines.size} machines (search=\"$search\", status=$status)")

        // nothing to show, the message depends on if there's no machines at all or just no matches
        when {
            machines.isNotEmpty() -> view.hideEmptyMessage()
            allMachines.isEmpty() -> view.showEmptyMessage("No machines yet.")
            else -> view.showEmptyMessage("No machines match your search or filter.")
        }
    }

    companion object {
        // plain Kotlin with no Android in it, so I can unit test it
        // a machine has to match the search AND the status to show up,
        // the search looks at the name or location and ignores capitals ("washer" finds "Washer 1")
        fun filterMachines(machines: List<MachineModel>, search: String, status: MachineStatus?): List<MachineModel> {
            return machines.filter { machine ->
                val matchesSearch =
                    machine.name.contains(search, ignoreCase = true) ||
                        machine.location.contains(search, ignoreCase = true)

                val matchesStatus =
                    status == null || machine.status == status

                matchesSearch && matchesStatus
            }
        }
    }
}
