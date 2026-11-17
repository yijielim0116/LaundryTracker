package org.setu.laundrytracker.activities

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.setu.laundrytracker.R
import timber.log.Timber

// Map screen for picking where a machine is - same idea as MapPickerActivity in my PlacedMark lab,
// but with Google Maps instead of osmdroid
class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    // the spot I picked, the marker sits on it
    private lateinit var selectedPoint: LatLng
    private var marker: Marker? = null
    private var selectedTitle = "Machine location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map_picker)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Set Location"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // starts in Waterford (same spot as my lab) unless the form sends the machine's location
        // after rotating, use the spot I'd already picked instead
        val state = savedInstanceState ?: intent.extras
        val lat = state?.getDouble(EXTRA_LATITUDE, DEFAULT_LATITUDE) ?: DEFAULT_LATITUDE
        val lon = state?.getDouble(EXTRA_LONGITUDE, DEFAULT_LONGITUDE) ?: DEFAULT_LONGITUDE
        selectedPoint = LatLng(lat, lon)

        // the machine's name shows on the marker
        selectedTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Machine location"

        Timber.d("MapPickerActivity opened at lat=$lat, lon=$lon, title=$selectedTitle")

        // the map loads in the background and calls onMapReady when it's ready to use
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.confirmLocationButton).setOnClickListener {
            confirmLocation()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        map.uiSettings.isZoomControlsEnabled = true

        // draggable so I can also hold the pin and move it
        marker = map.addMarker(
            MarkerOptions()
                .position(selectedPoint)
                .title(selectedTitle)
                .draggable(true)
        )

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPoint, ZOOM))

        // a tap or a long press moves the pin there
        map.setOnMapClickListener { point -> setSelectedPoint(point) }
        map.setOnMapLongClickListener { point -> setSelectedPoint(point) }

        // dragging the pin, I only care about where it ends up
        map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(dragged: Marker) {}

            override fun onMarkerDrag(dragged: Marker) {}

            override fun onMarkerDragEnd(dragged: Marker) {
                setSelectedPoint(dragged.position)
            }
        })
    }

    private fun setSelectedPoint(point: LatLng) {
        selectedPoint = point
        marker?.position = point
        Timber.d("Picked lat=${point.latitude}, lon=${point.longitude}")
    }

    // sends the spot back to AddEditActivity
    private fun confirmLocation() {
        Timber.d("Location confirmed lat=${selectedPoint.latitude}, lon=${selectedPoint.longitude}")

        val result = intent
            .putExtra(EXTRA_LATITUDE, selectedPoint.latitude)
            .putExtra(EXTRA_LONGITUDE, selectedPoint.longitude)

        setResult(RESULT_OK, result)
        finish()
    }

    // keep the picked spot if the screen is rotated
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble(EXTRA_LATITUDE, selectedPoint.latitude)
        outState.putDouble(EXTRA_LONGITUDE, selectedPoint.longitude)
    }

    // up arrow just goes back to the form without changing the location
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_TITLE = "title"

        // Waterford, same default as my PlacedMark lab
        private const val DEFAULT_LATITUDE = 52.245954
        private const val DEFAULT_LONGITUDE = -7.139539

        // close enough to see the buildings
        private const val ZOOM = 17f
    }
}
