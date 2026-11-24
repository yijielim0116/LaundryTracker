package org.setu.laundrytracker.views.mappicker

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
import org.setu.laundrytracker.main.useNightStyleIfDark

// Map screen for picking where a machine is - draws the map and the pin the presenter tells it about,
// and passes taps and drags on to the presenter (same idea as MapPickerView in my PlacedMark lab,
// but with Google Maps instead of osmdroid)
class MapPickerView : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var presenter: MapPickerPresenter

    // null until the map has loaded
    private var map: GoogleMap? = null
    private var marker: Marker? = null

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

        // the presenter works out the start spot (from the form, or the one I'd picked before rotating)
        presenter = MapPickerPresenter(this, savedInstanceState)

        // the map loads in the background and calls onMapReady when it's ready to use
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        findViewById<Button>(R.id.confirmLocationButton).setOnClickListener {
            presenter.doConfirmLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        // dark map in dark mode
        googleMap.useNightStyleIfDark(this)

        // a tap or a long press moves the pin there
        googleMap.setOnMapClickListener { point -> presenter.doSelectPoint(point) }
        googleMap.setOnMapLongClickListener { point -> presenter.doSelectPoint(point) }

        // dragging the pin, I only care about where it ends up
        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(dragged: Marker) {}

            override fun onMarkerDrag(dragged: Marker) {}

            override fun onMarkerDragEnd(dragged: Marker) {
                presenter.doSelectPoint(dragged.position)
            }
        })

        presenter.doMapReady()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        presenter.doCancel()
        return true
    }

    // ---- things the presenter tells me to show ----

    fun showMap(center: LatLng, zoom: Float) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom))
    }

    // draggable so I can also hold the pin and move it
    fun showSelectedPoint(point: LatLng, title: String) {
        marker = map?.addMarker(
            MarkerOptions()
                .position(point)
                .title(title)
                .draggable(true)
        )
    }

    fun moveSelectedPoint(point: LatLng) {
        marker?.position = point
    }

    // the names of the extras the add/edit form sends and gets back
    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_TITLE = "title"
    }
}
