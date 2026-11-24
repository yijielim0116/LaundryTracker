package org.setu.laundrytracker.views.home

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.setu.laundrytracker.R
import org.setu.laundrytracker.activities.DrawerActivity

// Home screen - the first thing you see after logging in
// extends DrawerActivity so it gets the navigation drawer
class HomeView : DrawerActivity() {

    private lateinit var presenter: HomePresenter

    // Home is highlighted in the drawer on this screen
    override val currentNavItem = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // stops the content going under the status bar / nav bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // my toolbar becomes the action bar, the title comes from the label in the manifest
        val toolbar =
            findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        setUpDrawer(toolbar)

        presenter = HomePresenter(this)

        findViewById<Button>(R.id.viewMachinesButton).setOnClickListener {
            presenter.doViewMachines()
        }

        presenter.doShowUser()
    }

    fun showLoggedInAs(email: String?) {
        findViewById<TextView>(R.id.loggedInText).text =
            "Logged in as $email"
    }
}
