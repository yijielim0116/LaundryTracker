package org.setu.laundrytracker.views.addedit

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.setu.laundrytracker.R
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType

// the add/edit screen only sets up the controls and passes what I press on to the presenter,
// the presenter decides what to do and tells this class what to show (same as AddEditView in my PlacedMark lab)
class AddEditView : AppCompatActivity() {

    private lateinit var presenter: AddEditPresenter

    private lateinit var nameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var typeGroup: RadioGroup
    private lateinit var statusGroup: RadioGroup
    private lateinit var cyclePicker: NumberPicker
    private lateinit var cycleSection: LinearLayout
    private lateinit var machineImage: ImageView
    private lateinit var mapLocationText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_edit)

        // my toolbar becomes the action bar, and the up arrow shows on the left
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Add Machine"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameInput =
            findViewById(R.id.nameInput)

        locationInput =
            findViewById(R.id.locationInput)

        typeGroup =
            findViewById(R.id.typeGroup)

        statusGroup =
            findViewById(R.id.statusGroup)

        cyclePicker =
            findViewById(R.id.cyclePicker)

        cycleSection =
            findViewById(R.id.cycleSection)

        machineImage =
            findViewById(R.id.machineImage)

        mapLocationText =
            findViewById(R.id.mapLocationText)

        setUpCyclePicker()

        // the controls have to exist before this, because the presenter can ask the view
        // to show an existing machine straight away when it is created
        presenter = AddEditPresenter(this)

        // put back the photo / camera file / map spot if Android rebuilt the screen
        if (savedInstanceState != null) {
            presenter.restoreState(savedInstanceState)
        }

        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            presenter.doSelectImage()
        }

        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            presenter.doTakePhoto()
        }

        findViewById<Button>(R.id.setLocationButton).setOnClickListener {
            presenter.doSetLocation(nameInput.text.toString())
        }

        // the presenter decides if the cycle picker shows for the ticked status
        statusGroup.setOnCheckedChangeListener { _, _ ->
            presenter.doStatusChanged(selectedStatus())
        }

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            presenter.doAddOrSave(
                nameInput.text.toString(),
                locationInput.text.toString(),
                selectedType(),
                selectedStatus(),
                selectedCycleMinutes()
            )
        }

        findViewById<Button>(R.id.cancelButton).setOnClickListener {
            presenter.doCancel()
        }
    }

    // up arrow on the toolbar does the same as Cancel
    // (finish() keeps the list's search and filter instead of opening a new list screen)
    override fun onSupportNavigateUp(): Boolean {
        presenter.doCancel()
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    // ---- things the presenter tells me to show ----

    fun showEditMode() {
        supportActionBar?.title = "Edit Machine"
    }

    // fills the form with the machine that is being edited
    fun showMachine(machine: MachineModel) {
        nameInput.setText(machine.name)
        locationInput.setText(machine.location)

        // tick the right radio buttons for the type and status
        typeGroup.check(
            when (machine.type) {
                MachineType.WASHER -> R.id.washerRadio
                MachineType.DRYER -> R.id.dryerRadio
            }
        )

        statusGroup.check(
            when (machine.status) {
                MachineStatus.AVAILABLE -> R.id.availableRadio
                MachineStatus.IN_USE -> R.id.inUseRadio
                MachineStatus.OUT_OF_ORDER -> R.id.outOfOrderRadio
            }
        )

        cyclePicker.value = minutesToIndex(machine.cycleMinutes)
    }

    fun showCycleSection(visible: Boolean) {
        cycleSection.visibility = if (visible) View.VISIBLE else View.GONE
    }

    // shows the photo, or the gallery icon if there isn't one
    fun showImage(uri: Uri?) {
        if (uri != null) {
            // clear it first, otherwise setImageURI sometimes skips redrawing (after the camera)
            machineImage.setImageDrawable(null)
            machineImage.setImageURI(uri)
        } else {
            machineImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    // 5 decimal places is about 1 metre, plenty for finding a washing machine
    fun showMapLocation(latitude: Double?, longitude: Double?) {
        mapLocationText.text =
            if (latitude != null && longitude != null) {
                "Lat %.5f, Lng %.5f".format(latitude, longitude)
            } else {
                "No location set yet"
            }
    }

    fun showNameError(message: String) {
        nameInput.error = message
    }

    fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // ---- reading the controls (only the view knows about radio buttons and the picker) ----

    // the picker goes 10, 15, 20 ... 120 minutes
    // NumberPicker only counts 0, 1, 2 ... so I use displayedValues to show the minutes instead
    private fun setUpCyclePicker() {
        val minutes = (MIN_MINUTES..MAX_MINUTES step STEP_MINUTES).toList()

        cyclePicker.minValue = 0
        cyclePicker.maxValue = minutes.size - 1
        cyclePicker.displayedValues = minutes.map { "$it min" }.toTypedArray()
        // stops it jumping from 120 back round to 10
        cyclePicker.wrapSelectorWheel = false
        // start on 60 min for a new machine
        cyclePicker.value = minutesToIndex(60)
    }

    // e.g. 10 min -> 0, 15 min -> 1, 60 min -> 10
    private fun minutesToIndex(minutes: Int): Int {
        val index = (minutes - MIN_MINUTES) / STEP_MINUTES
        return index.coerceIn(0, cyclePicker.maxValue)
    }

    // the other way round, picker position -> minutes
    private fun selectedCycleMinutes(): Int {
        return MIN_MINUTES + cyclePicker.value * STEP_MINUTES
    }

    private fun selectedType(): MachineType {
        return if (typeGroup.checkedRadioButtonId == R.id.dryerRadio) {
            MachineType.DRYER
        } else {
            MachineType.WASHER
        }
    }

    private fun selectedStatus(): MachineStatus {
        return when (statusGroup.checkedRadioButtonId) {
            R.id.inUseRadio -> MachineStatus.IN_USE
            R.id.outOfOrderRadio -> MachineStatus.OUT_OF_ORDER
            else -> MachineStatus.AVAILABLE
        }
    }

    // limits for the cycle length picker
    companion object {
        private const val MIN_MINUTES = 10
        private const val MAX_MINUTES = 120
        private const val STEP_MINUTES = 5
    }
}
