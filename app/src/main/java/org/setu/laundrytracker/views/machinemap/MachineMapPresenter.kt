package org.setu.laundrytracker.views.machinemap

import android.content.Intent
import com.google.android.gms.maps.model.LatLng
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.views.addedit.AddEditView
import timber.log.Timber

// works out which machines go on the map, where the camera goes and what a tapped pin opens
// the view only draws the map and the pins it is told about (same idea as MapListPresenter in my PlacedMark lab)
class MachineMapPresenter(private val view: MachineMapView) {

    // the map loads in the background, the view tells me when it's ready
    fun doMapReady() {
        Timber.d("Machine map ready")
        showMachines()
    }

    // coming back from editing a machine, show the pins again so moved / new ones show up
    fun doResume() {
        showMachines()

        // Firestore loads in the background, so put the pins on again when the machines change
        AppData.machines.setOnChangeListener { showMachines() }
    }

    fun doPause() {
        AppData.machines.setOnChangeListener(null)
    }

    // tapping a pin's pop-up opens that machine in the add/edit form
    fun doOpenMachine(id: Long) {
        Timber.d("Map pin tapped, opening machine id=$id")

        val intent = Intent(view, AddEditView::class.java)
        intent.putExtra("id", id)
        view.startActivity(intent)
    }

    // only machines with a location can go on the map
    private fun showMachines() {
        // nothing to draw on until the map has loaded, doMapReady calls this again then
        if (!view.isMapReady()) return

        val machines = AppData.machines.findAll().filter { it.hasLocation() }
        Timber.d("Showing ${machines.size} machines on the map")

        view.showMachines(machines)
        view.showNoLocationsMessage(machines.isEmpty())

        val points = machines.map { LatLng(it.latitude!!, it.longitude!!) }

        // zoom so every pin fits on the screen
        when (points.size) {
            // nothing to show, so just show Waterford like the location picker does
            0 -> view.showArea(WATERFORD, EMPTY_ZOOM)

            // one pin, a box around one point would zoom in way too far
            1 -> view.showArea(points[0], SINGLE_ZOOM)

            else -> view.showAllPoints(points)
        }
    }

    companion object {
        // same Waterford spot as the map picker / my lab
        private val WATERFORD = LatLng(52.245954, -7.139539)
        private const val EMPTY_ZOOM = 15f
        private const val SINGLE_ZOOM = 17f
    }
}
