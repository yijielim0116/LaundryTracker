package org.setu.laundrytracker.views.mappicker

import android.app.Activity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

// works out where the map starts, keeps the spot I picked and sends it back when I confirm
// the view only draws the map and the pin (same as MapPickerPresenter in my PlacedMark lab)
class MapPickerPresenter(private val view: MapPickerView, savedState: Bundle?) {

    // the spot I picked, the pin sits on it
    private var selectedPoint: LatLng

    // the machine's name, shown on the pin
    private val selectedTitle: String

    init {
        // starts in Waterford (same spot as my lab) unless the form sends the machine's location
        // after rotating, use the spot I'd already picked instead
        val state = savedState ?: view.intent.extras
        val lat = state?.getDouble(MapPickerView.EXTRA_LATITUDE, DEFAULT_LATITUDE) ?: DEFAULT_LATITUDE
        val lon = state?.getDouble(MapPickerView.EXTRA_LONGITUDE, DEFAULT_LONGITUDE) ?: DEFAULT_LONGITUDE
        selectedPoint = LatLng(lat, lon)

        selectedTitle = view.intent.getStringExtra(MapPickerView.EXTRA_TITLE) ?: "Machine location"

        Timber.d("MapPickerView opened at lat=$lat, lon=$lon, title=$selectedTitle")
    }

    // the map loads in the background, the view tells me when it's ready
    fun doMapReady() {
        view.showMap(selectedPoint, ZOOM)
        view.showSelectedPoint(selectedPoint, selectedTitle)
    }

    // a tap, a long press or dragging the pin moves the picked spot
    fun doSelectPoint(point: LatLng) {
        selectedPoint = point
        view.moveSelectedPoint(point)
        Timber.d("Picked lat=${point.latitude}, lon=${point.longitude}")
    }

    // sends the spot back to the add/edit form
    fun doConfirmLocation() {
        Timber.d("Location confirmed lat=${selectedPoint.latitude}, lon=${selectedPoint.longitude}")

        val result = view.intent
            .putExtra(MapPickerView.EXTRA_LATITUDE, selectedPoint.latitude)
            .putExtra(MapPickerView.EXTRA_LONGITUDE, selectedPoint.longitude)

        view.setResult(Activity.RESULT_OK, result)
        view.finish()
    }

    // up arrow just goes back to the form without changing the location
    fun doCancel() {
        view.finish()
    }

    // keep the picked spot if the screen is rotated
    fun saveState(outState: Bundle) {
        outState.putDouble(MapPickerView.EXTRA_LATITUDE, selectedPoint.latitude)
        outState.putDouble(MapPickerView.EXTRA_LONGITUDE, selectedPoint.longitude)
    }

    companion object {
        // Waterford, same default as my PlacedMark lab
        private const val DEFAULT_LATITUDE = 52.245954
        private const val DEFAULT_LONGITUDE = -7.139539

        // close enough to see the buildings
        private const val ZOOM = 17f
    }
}
