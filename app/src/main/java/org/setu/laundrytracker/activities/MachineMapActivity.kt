package org.setu.laundrytracker.activities

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import timber.log.Timber

// Map screen - shows every machine that has a location as a pin
// extends DrawerActivity so it gets the navigation drawer like Home and Machines
class MachineMapActivity : DrawerActivity(), OnMapReadyCallback {

    // Map is highlighted in the drawer on this screen
    override val currentNavItem = R.id.nav_map

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

        // the map loads in the background and calls onMapReady when it's ready
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.uiSettings.isZoomControlsEnabled = true

        Timber.d("Machine map ready")
        showMachines()
    }

    // coming back from editing a machine, redraw so moved / new pins show up
    override fun onResume() {
        super.onResume()
        showMachines()
    }

    // puts a pin on the map for every machine that has a location
    private fun showMachines() {
        val googleMap = map ?: return

        googleMap.clear()

        val machines = AppData.machines.findAll().filter { it.hasLocation() }
        Timber.d("Showing ${machines.size} machines on the map")

        val points = machines.map { machine ->
            val point = LatLng(machine.latitude!!, machine.longitude!!)

            googleMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .title(machine.name)
            )

            point
        }

        noLocationsText.visibility = if (points.isEmpty()) View.VISIBLE else View.GONE

        moveCamera(googleMap, points)
    }

    // zooms so every pin fits on the screen
    private fun moveCamera(googleMap: GoogleMap, points: List<LatLng>) {
        when (points.size) {
            // nothing to show, so just show Waterford like the location picker does
            0 -> googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(WATERFORD, EMPTY_ZOOM))

            // one pin, a bounds box around one point would zoom in way too far
            1 -> googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points[0], SINGLE_ZOOM))

            else -> {
                val bounds = LatLngBounds.builder()
                points.forEach { bounds.include(it) }

                // the map has to be laid out before it knows how big the screen is,
                // so I wait for it to load before fitting the box
                googleMap.setOnMapLoadedCallback {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), BOUNDS_PADDING))
                }
            }
        }
    }

    companion object {
        // same Waterford spot as MapPickerActivity / my lab
        private val WATERFORD = LatLng(52.245954, -7.139539)
        private const val EMPTY_ZOOM = 15f
        private const val SINGLE_ZOOM = 17f

        // space around the pins in pixels so they're not right on the edge
        private const val BOUNDS_PADDING = 150
    }
}
