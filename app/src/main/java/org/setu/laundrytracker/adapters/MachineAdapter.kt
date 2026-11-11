package org.setu.laundrytracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.setu.laundrytracker.R
import org.setu.laundrytracker.models.MachineModel
import org.setu.laundrytracker.models.MachineStatus
import org.setu.laundrytracker.models.MachineType

// adapter for the RecyclerView, it turns each machine into a row (item_machine.xml)
// same idea as the PlacedMarkAdapter in my lab
class MachineAdapter(
    private var machines: List<MachineModel>,
    // what to do when Edit / Delete is pressed, the list screen passes these in
    private val onEdit: (MachineModel) -> Unit,
    private val onDelete: (MachineModel) -> Unit
) : RecyclerView.Adapter<MachineAdapter.MachineViewHolder>() {

    // holds the views of one row so I only have to findViewById once per row
    class MachineViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val nameText: TextView =
            itemView.findViewById(R.id.nameText)

        val locationText: TextView =
            itemView.findViewById(R.id.locationText)

        val statusText: TextView =
            itemView.findViewById(R.id.statusText)

        val editButton: Button =
            itemView.findViewById(R.id.editButton)

        val deleteButton: Button =
            itemView.findViewById(R.id.deleteButton)
    }

    // makes a new empty row from item_machine.xml
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MachineViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_machine,
                parent,
                false
            )

        return MachineViewHolder(view)
    }

    // fills a row with one machine's details (rows get reused when scrolling)
    override fun onBindViewHolder(
        holder: MachineViewHolder,
        position: Int
    ) {

        val machine = machines[position]

        holder.nameText.text =
            "${machine.id}: ${machine.name}"

        holder.locationText.text =
            machine.location

        holder.statusText.text =
            statusLine(machine)

        holder.editButton.setOnClickListener {
            onEdit(machine)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(machine)
        }
    }

    // how many rows the RecyclerView needs
    override fun getItemCount(): Int {
        return machines.size
    }

    // gives the adapter a new list (after search, filter, add, edit or delete) and redraws
    fun updateMachines(newMachines: List<MachineModel>) {
        machines = newMachines
        notifyDataSetChanged()
    }

    // only add the cycle length for machines that are in use, it doesn't mean anything otherwise
    private fun statusLine(machine: MachineModel): String {
        val line = "${typeLabel(machine.type)} · ${statusLabel(machine.status)}"

        return if (machine.status == MachineStatus.IN_USE) {
            "$line · ${machine.cycleMinutes} min"
        } else {
            line
        }
    }

    // the enum names (WASHER, DRYER) look ugly on screen so I change them to normal words
    private fun typeLabel(type: MachineType): String {
        return when (type) {
            MachineType.WASHER -> "Washer"
            MachineType.DRYER -> "Dryer"
        }
    }

    // same thing for the status
    private fun statusLabel(status: MachineStatus): String {
        return when (status) {
            MachineStatus.AVAILABLE -> "Available"
            MachineStatus.IN_USE -> "In use"
            MachineStatus.OUT_OF_ORDER -> "Out of order"
        }
    }
}
