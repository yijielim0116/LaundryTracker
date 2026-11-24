package org.setu.laundrytracker.activities

import android.content.Intent
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import org.setu.laundrytracker.R
import org.setu.laundrytracker.main.AppData
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.main.ThemeSetting
import org.setu.laundrytracker.views.addedit.AddEditView
import org.setu.laundrytracker.views.machinelist.MachineListView
import timber.log.Timber

/**
 * The screens with the navigation drawer (Home, Machines and Map) extend this,
 * so the drawer code is only written once instead of copied into both.
 */
// abstract = I never open a DrawerActivity by itself, only the screens that extend it
abstract class DrawerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView

    // each screen says which drawer item it is, so that one gets highlighted
    protected abstract val currentNavItem: Int

    // call this in onCreate after setSupportActionBar, the layout needs a drawerLayout and navView
    protected fun setUpDrawer(toolbar: Toolbar) {
        drawerLayout =
            findViewById(R.id.drawerLayout)

        navView =
            findViewById(R.id.navView)

        // the ☰ button on the toolbar, it opens/closes the drawer (and animates into an arrow)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setCheckedItem(currentNavItem)

        val header = navView.getHeaderView(0)

        // show who is logged in at the top of the drawer
        header.findViewById<TextView>(R.id.drawerEmail)
            .text = "Logged in as ${Session.currentEmail()}"

        // the drawer goes up behind the status bar, so push the header down by the status bar's height
        // (otherwise the logo sits under the clock)
        val headerTopPadding = header.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(header) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, headerTopPadding + statusBar.top, v.paddingRight, v.paddingBottom)
            insets
        }

        navView.setNavigationItemSelectedListener { item ->
            drawerLayout.closeDrawer(GravityCompat.START)
            onDrawerItemSelected(item.itemId)
            true
        }

        // Back closes the drawer first if it's open, only turned on while the drawer is open
        val closeDrawerOnBack = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
        onBackPressedDispatcher.addCallback(this, closeDrawerOnBack)

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: android.view.View) {
                closeDrawerOnBack.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: android.view.View) {
                closeDrawerOnBack.isEnabled = false
            }
        })
    }

    private fun onDrawerItemSelected(itemId: Int) {
        Timber.d("Drawer item tapped: ${resources.getResourceEntryName(itemId)}")

        // tapping the screen I'm already on just closes the drawer
        if (itemId == currentNavItem) {
            return
        }

        when (itemId) {
            R.id.nav_home -> {
                // CLEAR_TOP goes back to the Home that's already open instead of stacking a new one
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }

            R.id.nav_machines ->
                startActivity(Intent(this, MachineListView::class.java))

            R.id.nav_map ->
                startActivity(Intent(this, MachineMapActivity::class.java))

            R.id.nav_add_machine ->
                startActivity(Intent(this, AddEditView::class.java))

            R.id.nav_dark_mode ->
                showDarkModeChoice()

            R.id.nav_about ->
                showAbout()

            R.id.nav_logout ->
                logOut()
        }
    }

    // pop up with the 3 choices, the one I picked last time is ticked
    private fun showDarkModeChoice() {
        val options = arrayOf("Follow the phone", "Light", "Dark")

        AlertDialog.Builder(this)
            .setTitle("Dark mode")
            .setSingleChoiceItems(options, ThemeSetting.saved(this)) { dialog, which ->
                Timber.d("Dark mode set to ${options[which]}")
                ThemeSetting.save(this, which)
                dialog.dismiss()
                // every open screen gets redrawn in the new mode
                ThemeSetting.apply(which)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // small pop up with info about the app (it used to be in the machine list menu)
    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("About Laundry Tracker")
            .setMessage("Check if the laundry machines are free before going down, and keep track of your wash and dry cycles.")
            .setPositiveButton("OK", null)
            .show()
    }

    // moved here from MainActivity so Log out works from any screen with the drawer
    private fun logOut() {
        Timber.d("Logging out ${Session.currentEmail()}")
        Session.logout()
        // stop listening to this user's machines in Firestore
        AppData.signedOut()

        // CLEAR_TASK so Back on the login screen can't get back into the app
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
