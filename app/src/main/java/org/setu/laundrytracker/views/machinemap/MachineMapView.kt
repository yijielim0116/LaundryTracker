package org.setu.laundrytracker.views.machinemap

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.setu.laundrytracker.R
import org.setu.laundrytracker.activities.DrawerActivity
import org.setu.laundrytracker.main.useNightStyleIfDark
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.label

// Map screen - draws the map and the pins the presenter tells it about,
// and passes a tapped pin on to the presenter (same idea as MapListView in my PlacedMark lab)
// extends DrawerActivity so it gets the navigation drawer like Home and Machines
class MachineMapView : DrawerActivity(), OnMapReadyCallback {

    // Map is highlighted in the drawer on this screen
    override val currentNavItem = R.id.nav_map

    private lateinit var presenter: MachineMapPresenter

    // null until the map has loaded
    private var map: GoogleMap? = null
    private lateinit var noLocationsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_machine_map)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        setUpDrawer(toolbar)

        noLocationsText =
            findViewById(R.id.noLocationsText)

        presenter = MachineMapPresenter(this)

        // the map loads in the background and calls onMapReady when it's ready
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true
        // dark map in dark mode
        googleMap.useNightStyleIfDark(this)
        // the campus buildings have floors, but my machines don't have a floor so I hide that picker
        googleMap.uiSettings.isIndoorLevelPickerEnabled = false

        // tapping a pin shows its name + status, tapping that little box opens the machine
        googleMap.setOnInfoWindowClickListener { marker ->
            val id = marker.tag as? Long ?: return@setOnInfoWindowClickListener
            presenter.doOpenMachine(id)
        }

        presenter.doMapReady()
    }

    override fun onResume() {
        super.onResume()
        presenter.doResume()
    }

    override fun onPause() {
        presenter.doPause()
        super.onPause()
    }

    // ---- things the presenter tells me to show ----

    fun isMapReady(): Boolean {
        return map != null
    }

    // puts a pin on the map for every machine it gives me
    fun showMachines(machines: List<MachineModel>) {
        val googleMap = map ?: return

        googleMap.clear()

        machines.forEach { machine ->
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(machine.latitude!!, machine.longitude!!))
                    .title(machine.name)
                    // same words as the list, e.g. "Washer · Available"
                    .snippet("${machine.type.label()} · ${machine.status.label()}")
                    .icon(BitmapDescriptorFactory.defaultMarker(statusHue(machine.status)))
            )

            // remember which machine this pin is, so tapping it can open the right one
            marker?.tag = machine.id
        }
    }

    fun showNoLocationsMessage(show: Boolean) {
        noLocationsText.visibility = if (show) View.VISIBLE else View.GONE
    }

    // centre the map on one spot
    fun showArea(center: LatLng, zoom: Float) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom))
    }

    // zoom so every point fits on the screen
    fun showAllPoints(points: List<LatLng>) {
        val googleMap = map ?: return

        val bounds = LatLngBounds.builder()
        points.forEach { bounds.include(it) }

        // the map has to be laid out before it knows how big the screen is,
        // so I wait for it to load before fitting the box
        googleMap.setOnMapLoadedCallback {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), BOUNDS_PADDING))
        }
    }

    // green = free, orange = someone's using it, red = broken, so you can tell at a glance
    private fun statusHue(status: MachineStatus): Float {
        return when (status) {
            MachineStatus.AVAILABLE -> BitmapDescriptorFactory.HUE_GREEN
            MachineStatus.IN_USE -> BitmapDescriptorFactory.HUE_ORANGE
            MachineStatus.OUT_OF_ORDER -> BitmapDescriptorFactory.HUE_RED
        }
    }

    companion object {
        // space around the pins in pixels so they're not right on the edge
        private const val BOUNDS_PADDING = 150
    }
}
