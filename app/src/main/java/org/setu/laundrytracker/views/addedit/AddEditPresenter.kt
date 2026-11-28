package org.setu.laundrytracker.views.addedit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import org.setu.laundrytracker.views.mappicker.MapPickerView
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType
import timber.log.Timber
import java.io.File

// all the decisions for the add/edit screen live here, the view just passes on what I pressed
// and shows whatever the presenter tells it to (same as AddEditPresenter in my PlacedMark lab)
class AddEditPresenter(private val view: AddEditView) {

    // null when I'm adding a new machine, the id of the machine when I'm editing one
    private var editingId: Long? = null

    // the photo I picked or took, null until there is one
    private var imageUri: Uri? = null

    // the spot picked on the map, null until I pick one
    private var latitude: Double? = null
    private var longitude: Double? = null

    // where the camera saves the photo it's taking right now
    private var cameraFile: File? = null
    private var cameraUri: Uri? = null

    private lateinit var imageIntentLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cameraIntentLauncher: ActivityResultLauncher<Uri>
    private lateinit var mapIntentLauncher: ActivityResultLauncher<Intent>

    init {
        // these have to be registered while the view is being created
        registerImagePickerCallback()
        registerCameraCallback()
        registerMapCallback()

        // the machine list sends the id when Edit is pressed, -1 means no id was sent
        editingId =
            view.intent.getLongExtra("id", -1L)
                .takeIf { it != -1L }

        if (editingId != null) {
            Timber.d("Form opened to edit machine id=$editingId")
            view.showEditMode()
            loadExistingMachine(editingId!!)
        } else {
            Timber.d("Form opened to add a new machine")
        }
    }

    // called by the view when Save is pressed, works out if it's a new machine or an edit
    fun doAddOrSave(name: String, location: String, type: MachineType, status: MachineStatus, cycleMinutes: Int) {

        val cleanName = name.trim()
        val cleanLocation = location.trim()

        // the other machines' names, so I can stop two machines having the same name
        // (leave out the one I'm editing, otherwise saving it without changes would say it's taken)
        val otherNames = AppData.machines.findAll()
            .filter { it.id != editingId }
            .map { it.name }

        val nameProblem = nameError(cleanName, otherNames)
        if (nameProblem != null) {
            Timber.d("Save blocked: $nameProblem")
            view.showNameError(nameProblem)
            return
        }

        val locationProblem = locationError(cleanLocation)
        if (locationProblem != null) {
            Timber.d("Save blocked: $locationProblem")
            view.showLocationError(locationProblem)
            return
        }

        val machine = MachineModel(
            // keep the same id when editing so the store knows which machine to replace
            id = editingId ?: 0L,
            name = cleanName,
            location = cleanLocation,
            type = type,
            status = status,
            cycleMinutes = cycleMinutes,
            imageUri = imageUri?.toString(),
            latitude = latitude,
            longitude = longitude
        )

        // no id means it's a new machine, otherwise update the one I'm editing
        if (editingId == null) {
            AppData.machines.create(machine)
            view.showMessage("Machine created")
        } else {
            AppData.machines.update(machine)
            view.showMessage("Machine updated")
        }

        view.finish()
    }

    // Cancel or the up arrow, just closes the form (nothing gets saved)
    fun doCancel() {
        Timber.d("Add/edit cancelled")
        view.finish()
    }

    // the cycle length only matters when someone is using the machine,
    // so the picker only shows for In use (Available / Out of order hide it)
    fun doStatusChanged(status: MachineStatus) {
        view.showCycleSection(status == MachineStatus.IN_USE)
    }

    // only images show up in the picker
    fun doSelectImage() {
        imageIntentLauncher.launch(arrayOf("image/*"))
    }

    // makes an empty file in filesDir/images and gives the camera a content:// uri for it
    // (FileProvider turns my private file into a uri the camera app is allowed to write to)
    fun doTakePhoto() {
        val imagesDir = File(view.filesDir, "images")
        imagesDir.mkdirs()

        val file = File(imagesDir, "machine_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(view, "${view.packageName}.fileprovider", file)

        cameraFile = file
        cameraUri = uri
        cameraIntentLauncher.launch(uri)
    }

    // opens the map, starting on the machine's spot if it already has one (otherwise the map uses Waterford)
    // the view gives me the name that's typed in so it shows on the pin
    fun doSetLocation(name: String) {
        Timber.d("Set Location pressed")

        val intent = Intent(view, MapPickerView::class.java)

        val lat = latitude
        val lon = longitude

        if (lat != null && lon != null) {
            intent.putExtra(MapPickerView.EXTRA_LATITUDE, lat)
            intent.putExtra(MapPickerView.EXTRA_LONGITUDE, lon)
        }

        val cleanName = name.trim()
        if (cleanName.isNotEmpty()) {
            intent.putExtra(MapPickerView.EXTRA_TITLE, cleanName)
        }

        mapIntentLauncher.launch(intent)
    }

    // Android can close the screen while the camera or map is open (e.g. low memory),
    // so the view hands me its Bundle and I keep the photo, camera file and map spot in it
    fun saveState(outState: Bundle) {
        outState.putString(KEY_IMAGE_URI, imageUri?.toString())
        outState.putString(KEY_CAMERA_PATH, cameraFile?.absolutePath)

        // Bundle can't hold a null Double, so only save the location if there is one
        val lat = latitude
        val lon = longitude
        if (lat != null && lon != null) {
            outState.putDouble(KEY_LATITUDE, lat)
            outState.putDouble(KEY_LONGITUDE, lon)
        }
    }

    fun restoreState(state: Bundle) {
        state.getString(KEY_CAMERA_PATH)?.let { path ->
            cameraFile = File(path)
            cameraUri = FileProvider.getUriForFile(view, "${view.packageName}.fileprovider", File(path))
        }

        imageUri = state.getString(KEY_IMAGE_URI)?.let { Uri.parse(it) }
        view.showImage(imageUri)

        if (state.containsKey(KEY_LATITUDE) && state.containsKey(KEY_LONGITUDE)) {
            latitude = state.getDouble(KEY_LATITUDE)
            longitude = state.getDouble(KEY_LONGITUDE)
        }
        view.showMapLocation(latitude, longitude)
    }

    // fills the form with the machine's current details so I can change them
    private fun loadExistingMachine(id: Long) {

        val machine =
            AppData.machines.findOne(id)

        if (machine == null) {
            Timber.w("Tried to edit machine id=$id but it was not found")
            view.showMessage("Machine not found")
            view.finish()
            return
        }

        imageUri = machine.imageUri?.let { Uri.parse(it) }
        latitude = machine.latitude
        longitude = machine.longitude

        view.showMachine(machine)
        view.showCycleSection(machine.status == MachineStatus.IN_USE)
        view.showImage(imageUri)
        view.showMapLocation(latitude, longitude)
    }

    // opens the phone's file picker, uri comes back null if I cancel
    private fun registerImagePickerCallback() {
        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                if (uri != null) {
                    // ask to keep access to the photo so it still loads after the app restarts
                    try {
                        view.contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                        // some pickers don't allow it, the photo still shows for now
                        Timber.w("Could not keep access to $uri")
                    }

                    Timber.d("Picked image $uri")
                    imageUri = uri
                    view.showImage(uri)
                } else {
                    Timber.d("Image picker cancelled")
                }
            }
    }

    // opens the camera app, it saves the photo straight into cameraUri and tells me if it worked
    private fun registerCameraCallback() {
        cameraIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.TakePicture()) { saved: Boolean ->
                if (saved) {
                    Timber.d("Photo taken: $cameraUri")
                    imageUri = cameraUri
                    view.showImage(cameraUri)
                } else {
                    // cancelled, so get rid of the empty file the camera never wrote to
                    Timber.d("Camera cancelled")
                    cameraFile?.delete()
                }
            }
    }

    // when I confirm a spot on the map it comes back here with the latitude and longitude
    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data ?: return@registerForActivityResult

                    latitude = data.getDoubleExtra(MapPickerView.EXTRA_LATITUDE, 0.0)
                    longitude = data.getDoubleExtra(MapPickerView.EXTRA_LONGITUDE, 0.0)

                    Timber.d("Map location picked lat=$latitude, lon=$longitude")
                    view.showMapLocation(latitude, longitude)
                } else {
                    Timber.d("Map picker cancelled")
                }
            }
    }

    // keys for saving the photo and map spot when the screen gets recreated
    // + the form checks as plain functions so my unit tests can check them (null means it's fine)
    companion object {
        // same numbers as max_name_length / max_location_length in integers.xml
        const val MAX_NAME_LENGTH = 30
        const val MAX_LOCATION_LENGTH = 40

        fun nameError(name: String, otherNames: List<String>): String? {
            val clean = name.trim()
            return when {
                clean.isEmpty() -> "Name is required"
                clean.length > MAX_NAME_LENGTH -> "Name can't be longer than $MAX_NAME_LENGTH characters"
                // "washer 1" and "Washer 1" count as the same name
                otherNames.any { it.trim().equals(clean, ignoreCase = true) } ->
                    "A machine called $clean already exists"
                else -> null
            }
        }

        fun locationError(location: String): String? {
            val clean = location.trim()
            return when {
                clean.isEmpty() -> "Location is required"
                clean.length > MAX_LOCATION_LENGTH -> "Location can't be longer than $MAX_LOCATION_LENGTH characters"
                else -> null
            }
        }

        private const val KEY_IMAGE_URI = "imageUri"
        private const val KEY_CAMERA_PATH = "cameraPath"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }
}
