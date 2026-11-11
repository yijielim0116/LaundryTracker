package org.setu.laundrytracker.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.setu.laundrytracker.R
import org.setu.laundrytracker.adapters.MachineAdapter
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus

// Machine list screen - shows all the machines with search, the status filter and the menu
class MachineListActivity : AppCompatActivity() {

    // the RecyclerView shows the machines, the adapter turns each machine into a row
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MachineAdapter
    private lateinit var emptyText: TextView
    private lateinit var searchInput: EditText
    private lateinit var statusFilter: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_machine_list)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // use my Toolbar as the action bar, the title comes from the label in the manifest
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        // up arrow to go back to the home screen
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emptyText =
            findViewById(R.id.emptyText)

        recyclerView =
            findViewById(R.id.machinesRecyclerView)

        // start with an empty list, displayMachines() gives it the real machines
        // the adapter calls back here when Edit or Delete is pressed on a row
        adapter = MachineAdapter(
            machines = emptyList(),

            onEdit = { machine ->
                editMachine(machine)
            },

            onDelete = { machine ->
                deleteMachine(machine)
            }
        )

        // LinearLayoutManager = rows go one under the other
        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        searchInput =
            findViewById(R.id.searchInput)

        // redraw the list every time the search text changes
        searchInput.doAfterTextChanged {
            displayMachines()
        }

        statusFilter =
            findViewById(R.id.statusFilter)

        // redraw the list when a different status is picked
        statusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                displayMachines()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        displayMachines()
    }

    // up arrow on the toolbar, just closes this screen and goes back to home
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // puts my menu (res/menu/menu_main.xml) on the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // what happens when a menu item is tapped
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                openAddMachine()
                true
            }

            R.id.action_clear_filters -> {
                clearFilters()
                true
            }

            R.id.action_about -> {
                showAbout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // opens the form to add a new machine (from the Add item on the toolbar)
    private fun openAddMachine() {
        startActivity(
            Intent(this, AddEditActivity::class.java)
        )
    }

    // empties the search box and sets the drop-down back to All,
    // both of these redraw the list by themselves
    private fun clearFilters() {
        searchInput.setText("")
        statusFilter.setSelection(0)
    }

    // small pop up with info about the app
    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About Laundry Tracker")
            .setMessage("Check if the laundry machines are free before going down, and keep track of your wash and dry cycles.")
            .setPositiveButton("OK", null)
            .show()
    }

    // onResume runs when I come back from AddEditActivity,
    // so I reload the list here otherwise the new machine won't show
    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            displayMachines()
        }
    }

    // filters the machines and gives them to the adapter, the RecyclerView redraws the rows
    private fun displayMachines() {

        val allMachines = AppData.machines.findAll()

        // only keep the machines where the name or location has the search text in it
        // ignoreCase so "washer" still finds "Washer 1"
        val search = searchInput.text.toString().trim()

        // null means "All" so I don't filter by status
        val status = selectedStatus()

        // a machine has to match the search AND the status to show up
        val machines = allMachines.filter { machine ->
            val matchesSearch =
                machine.name.contains(search, ignoreCase = true) ||
                    machine.location.contains(search, ignoreCase = true)

            val matchesStatus =
                status == null || machine.status == status

            matchesSearch && matchesStatus
        }

        adapter.updateMachines(machines)

        // nothing to show, the message depends on if there's no machines at all or just no matches
        if (machines.isEmpty()) {
            emptyText.text =
                if (allMachines.isEmpty()) "No machines yet." else "No machines match your search or filter."
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE
        }
    }

    // Edit pressed on a row: open the form and send the machine's id so it knows which one to edit
    private fun editMachine(machine: MachineModel) {

        val intent =
            Intent(this, AddEditActivity::class.java)

        intent.putExtra("id", machine.id)

        startActivity(intent)
    }

    // Delete pressed on a row: remove it from the store then redraw the list
    private fun deleteMachine(machine: MachineModel) {

        AppData.machines.delete(machine.id)

        displayMachines()
    }

    // turns the drop-down position into a status, same order as status_filter_options in strings.xml
    private fun selectedStatus(): MachineStatus? {
        return when (statusFilter.selectedItemPosition) {
            1 -> MachineStatus.AVAILABLE
            2 -> MachineStatus.IN_USE
            3 -> MachineStatus.OUT_OF_ORDER
            else -> null
        }
    }
}
