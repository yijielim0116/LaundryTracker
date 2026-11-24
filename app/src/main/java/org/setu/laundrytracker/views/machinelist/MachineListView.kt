package org.setu.laundrytracker.views.machinelist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.setu.laundrytracker.R
import org.setu.laundrytracker.activities.DrawerActivity
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus

// Machine list screen - shows the machines, the search box, the status filter and the menu,
// and passes everything I press on to the presenter (same as MarkListView in my PlacedMark lab)
// extends DrawerActivity so it gets the navigation drawer too
class MachineListView : DrawerActivity() {

    // Machines is highlighted in the drawer on this screen
    override val currentNavItem = R.id.nav_machines

    private lateinit var presenter: MachineListPresenter

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
        // the ☰ button replaces the up arrow, Back still goes to the home screen
        setUpDrawer(toolbar)

        presenter = MachineListPresenter(this)

        emptyText =
            findViewById(R.id.emptyText)

        recyclerView =
            findViewById(R.id.machinesRecyclerView)

        // starts empty, the presenter gives it the machines in onResume
        // the adapter calls back here when Edit or Delete is pressed on a row
        adapter = MachineAdapter(
            machines = emptyList(),

            onEdit = { machine ->
                presenter.doEditMachine(machine)
            },

            onDelete = { machine ->
                presenter.doDeleteMachine(machine)
            }
        )

        // LinearLayoutManager = rows go one under the other
        recyclerView.layoutManager =
            LinearLayoutManager(this)

        recyclerView.adapter = adapter

        // the round + button opens the add form
        findViewById<FloatingActionButton>(R.id.addMachineFab).setOnClickListener {
            presenter.doAddMachine()
        }

        searchInput =
            findViewById(R.id.searchInput)

        // tell the presenter every time the search text changes
        searchInput.doAfterTextChanged {
            presenter.doSearchChanged(it.toString())
        }

        statusFilter =
            findViewById(R.id.statusFilter)

        // tell the presenter when a different status is picked
        statusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.doStatusFilterChanged(selectedStatus())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // puts my menu (res/menu/menu_main.xml) on the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // what happens when a menu item is tapped
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_filters -> {
                presenter.doClearFilters()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // onResume runs when I come back from the add/edit form, so the presenter shows the list again
    override fun onResume() {
        super.onResume()
        presenter.doResume()
    }

    override fun onPause() {
        presenter.doPause()
        super.onPause()
    }

    // ---- things the presenter tells me to show ----

    fun showMachines(machines: List<MachineModel>) {
        adapter.updateMachines(machines)
    }

    fun showEmptyMessage(message: String) {
        emptyText.text = message
        emptyText.visibility = View.VISIBLE
    }

    fun hideEmptyMessage() {
        emptyText.visibility = View.GONE
    }

    // empties the search box and sets the drop-down back to All,
    // both of these tell the presenter by themselves
    fun clearFilterControls() {
        searchInput.setText("")
        statusFilter.setSelection(0)
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
