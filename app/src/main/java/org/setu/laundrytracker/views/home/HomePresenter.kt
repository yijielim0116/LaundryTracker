package org.setu.laundrytracker.views.home

import android.content.Intent
import org.setu.laundrytracker.main.Session
import org.setu.laundrytracker.views.machinelist.MachineListView
import timber.log.Timber

// the home screen's logic, the view only has the button and the logged in text
class HomePresenter(private val view: HomeView) {

    fun doShowUser() {
        view.showLoggedInAs(Session.currentEmail())
    }

    // go to the list of all the machines
    fun doViewMachines() {
        Timber.d("View Machines pressed")
        view.startActivity(Intent(view, MachineListView::class.java))
    }
}
