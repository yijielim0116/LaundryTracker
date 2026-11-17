package org.setu.laundrytracker.activities

import android.content.Intent
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType
import java.io.File
import timber.log.Timber

class AddEditActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var typeGroup: RadioGroup
    private lateinit var statusGroup: RadioGroup
    private lateinit var cyclePicker: NumberPicker
    private lateinit var cycleSection: LinearLayout
    private lateinit var machineImage: ImageView
    private lateinit var mapLocationText: TextView

    // the spot picked on the map, null until I pick one (same as my PlacedMark lab)
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    // the photo I picked, null until I pick one (same as selectedImageUri in my PlacedMark lab)
    private var selectedImageUri: Uri? = null

    // null = adding a new machine, otherwise it's the id of the machine I'm editing
    private var editingId: Long? = null

    // opens the phone's file picker, uri comes back null if I cancel
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                // ask to keep access to the photo so it still loads after the app restarts
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // some pickers don't allow it, the photo still shows for now
                    Timber.w("Could not keep access to $uri")
                }

                Timber.d("Picked image $uri")
                selectedImageUri = uri
                machineImage.setImageURI(uri)
            } else {
                Timber.d("Image picker cancelled")
            }
        }

    // where the camera saves the photo it's taking right now
    private var cameraFile: File? = null
    private var cameraUri: Uri? = null

    // opens the camera app, it saves the photo straight into cameraUri and tells me if it worked
    // opens the map, and when I confirm a spot it comes back here with the latitude and longitude
    private val mapPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                selectedLatitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0.0)
                selectedLongitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0.0)

                Timber.d("Map location picked lat=$selectedLatitude, lon=$selectedLongitude")
                showMapLocation()
            } else {
                Timber.d("Map picker cancelled")
            }
        }

    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { saved: Boolean ->
            if (saved) {
                Timber.d("Photo taken: $cameraUri")
                selectedImageUri = cameraUri
                // clear it first, otherwise setImageURI sometimes skips redrawing
                machineImage.setImageDrawable(null)
                machineImage.setImageURI(cameraUri)
            } else {
                // cancelled, so get rid of the empty file the camera never wrote to
                Timber.d("Camera cancelled")
                cameraFile?.delete()
            }
        }

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

        // only images show up in the picker
        findViewById<Button>(R.id.selectImageButton).setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }

        mapLocationText =
            findViewById(R.id.mapLocationText)

        findViewById<Button>(R.id.setLocationButton).setOnClickListener {
            openMapPicker()
        }

        findViewById<Button>(R.id.takePhotoButton).setOnClickListener {
            openCamera()
        }

        setUpCyclePicker()

        // show or hide the cycle picker whenever a different status is ticked
        statusGroup.setOnCheckedChangeListener { _, _ ->
            updateCycleSection()
        }

        val saveButton =
            findViewById<Button>(R.id.saveButton)

        val cancelButton =
            findViewById<Button>(R.id.cancelButton)

        // the machine list sends the id when Edit is pressed, -1 means no id was sent
        editingId =
            intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {

            Timber.d("Form opened to edit machine id=$editingId")

            supportActionBar?.title = "Edit Machine"

            loadExistingMachine(editingId!!)
        } else {
            Timber.d("Form opened to add a new machine")
        }

        // Android can close this screen while the camera is open (e.g. low memory),
        // so put back the photo + camera file I saved in onSaveInstanceState
        if (savedInstanceState != null) {
            restorePhotoState(savedInstanceState)
        }

        saveButton.setOnClickListener {
            saveMachine()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    // up arrow on the toolbar, just closes the form and goes back to the list (nothing gets saved)
    // I use finish() so the list keeps its search and filter instead of opening a new main screen
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // fills the form with the machine's current details so I can change them
    private fun loadExistingMachine(id: Long) {

        val machine =
            AppData.machines.findOne(id)

        if (machine == null) {

            Toast.makeText(
                this,
                "Machine not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

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

        updateCycleSection()

        // show the photo this machine already has (the gallery icon is already there if it has none)
        selectedImageUri = machine.imageUri?.let { Uri.parse(it) }
        selectedImageUri?.let { machineImage.setImageURI(it) }

        // and the spot on the map, if it has one
        selectedLatitude = machine.latitude
        selectedLongitude = machine.longitude
        showMapLocation()
    }

    // opens the map, starting on the machine's spot if it already has one (otherwise the map uses Waterford)
    private fun openMapPicker() {
        val intent = Intent(this, MapPickerActivity::class.java)

        val lat = selectedLatitude
        val lon = selectedLongitude

        if (lat != null && lon != null) {
            intent.putExtra(MapPickerActivity.EXTRA_LATITUDE, lat)
            intent.putExtra(MapPickerActivity.EXTRA_LONGITUDE, lon)
        }

        // the name shows on the pin
        val name = nameInput.text.toString().trim()
        if (name.isNotEmpty()) {
            intent.putExtra(MapPickerActivity.EXTRA_TITLE, name)
        }

        Timber.d("Set Location pressed")
        mapPicker.launch(intent)
    }

    // 5 decimal places is about 1 metre, plenty for finding a washing machine
    private fun showMapLocation() {
        val lat = selectedLatitude
        val lon = selectedLongitude

        mapLocationText.text =
            if (lat != null && lon != null) {
                "Lat %.5f, Lng %.5f".format(lat, lon)
            } else {
                "No location set yet"
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_IMAGE_URI, selectedImageUri?.toString())
        outState.putString(KEY_CAMERA_PATH, cameraFile?.absolutePath)

        // Bundle can't hold a null Double, so only save the location if there is one
        val lat = selectedLatitude
        val lon = selectedLongitude
        if (lat != null && lon != null) {
            outState.putDouble(KEY_LATITUDE, lat)
            outState.putDouble(KEY_LONGITUDE, lon)
        }
    }

    private fun restorePhotoState(state: Bundle) {
        state.getString(KEY_CAMERA_PATH)?.let { path ->
            cameraFile = File(path)
            cameraUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", File(path))
        }

        selectedImageUri = state.getString(KEY_IMAGE_URI)?.let { Uri.parse(it) }
        selectedImageUri?.let { machineImage.setImageURI(it) }

        if (state.containsKey(KEY_LATITUDE) && state.containsKey(KEY_LONGITUDE)) {
            selectedLatitude = state.getDouble(KEY_LATITUDE)
            selectedLongitude = state.getDouble(KEY_LONGITUDE)
        }
        showMapLocation()
    }

    // makes an empty file in filesDir/images and gives the camera a content:// uri for it
    // (FileProvider turns my private file into a uri the camera app is allowed to write to)
    private fun openCamera() {
        val imagesDir = File(filesDir, "images")
        imagesDir.mkdirs()

        val file = File(imagesDir, "machine_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

        cameraFile = file
        cameraUri = uri
        takePhoto.launch(uri)
    }

    // the cycle length only matters when someone is using the machine,
    // so the picker only shows for In use (Available / Out of order hide it)
    private fun updateCycleSection() {
        cycleSection.visibility =
            if (selectedStatus() == MachineStatus.IN_USE) View.VISIBLE else View.GONE
    }

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

    private fun saveMachine() {

        val name =
            nameInput.text.toString().trim()

        val location =
            locationInput.text.toString().trim()

        if (name.isEmpty()) {
            Timber.d("Save blocked: name is empty")
            nameInput.error = "Name is required"
            return
        }

        // no id means it's a new machine, otherwise update the one I'm editing
        if (editingId == null) {

            val machine = MachineModel(
                name = name,
                location = location,
                type = selectedType(),
                status = selectedStatus(),
                cycleMinutes = selectedCycleMinutes(),
                imageUri = selectedImageUri?.toString(),
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )

            AppData.machines.create(machine)

            Toast.makeText(
                this,
                "Machine created",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // keep the same id so the store knows which machine to replace
            val machine = MachineModel(
                id = editingId!!,
                name = name,
                location = location,
                type = selectedType(),
                status = selectedStatus(),
                cycleMinutes = selectedCycleMinutes(),
                imageUri = selectedImageUri?.toString(),
                latitude = selectedLatitude,
                longitude = selectedLongitude
            )

            AppData.machines.update(machine)

            Toast.makeText(
                this,
                "Machine updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }

    // limits for the cycle length picker
    companion object {
        private const val MIN_MINUTES = 10
        private const val MAX_MINUTES = 120
        private const val STEP_MINUTES = 5

        // keys for saving the photo when the screen gets recreated
        private const val KEY_IMAGE_URI = "imageUri"
        private const val KEY_CAMERA_PATH = "cameraPath"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
}
