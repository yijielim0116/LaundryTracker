package org.setu.laundrytracker.models

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import timber.log.Timber

/**
 * Keeps one user's machines in Cloud Firestore, under users/{userId}/machines.
 */
// Firestore loads in the background, but MachineStore gives answers straight away,
// so I keep a copy of the machines here that Firestore's live listener keeps up to date
class MachineFirestoreStore(private val userId: String) : MachineStore {

    // each machine is one document, named after its id
    private val collection = FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("machines")

    private val machines = ArrayList<MachineModel>()
    private var onChange: (() -> Unit)? = null

    // runs every time something changes in Firestore (from this phone or anywhere else),
    // also works offline because Firestore keeps its own cache on the phone
    private val registration: ListenerRegistration =
        collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error, "Firestore listen failed for user $userId")
                return@addSnapshotListener
            }

            if (snapshot == null) {
                return@addSnapshotListener
            }

            machines.clear()
            machines.addAll(
                snapshot.documents
                    .mapNotNull { document -> document.data?.let { machineFromFirestore(it) } }
                    .sortedBy { it.id }
            )

            Timber.d("load: ${machines.size} machines from Firestore")
            onChange?.invoke()
        }

    override fun findAll(): List<MachineModel> {
        return machines
    }

    override fun create(machine: MachineModel) {
        // carry on from the biggest id, like my other stores
        machine.id = (machines.maxOfOrNull { it.id } ?: 0L) + 1
        // add it here too so it shows straight away, the listener confirms it a moment later
        machines.add(machine)

        collection.document(machine.id.toString())
            .set(machine.toFirestoreMap())
            .addOnFailureListener { Timber.e(it, "create failed for id=${machine.id}") }

        Timber.d("create: id=${machine.id}, name=${machine.name}")
    }

    override fun update(machine: MachineModel): Boolean {
        val foundIndex = machines.indexOfFirst { it.id == machine.id }
        if (foundIndex == -1) {
            Timber.w("update: no machine with id=${machine.id}")
            return false
        }

        machines[foundIndex] = machine

        collection.document(machine.id.toString())
            .set(machine.toFirestoreMap())
            .addOnFailureListener { Timber.e(it, "update failed for id=${machine.id}") }

        Timber.d("update: id=${machine.id}, name=${machine.name}")
        return true
    }

    override fun delete(id: Long): Boolean {
        val found = findOne(id)
        if (found == null) {
            Timber.w("delete: no machine with id=$id")
            return false
        }

        machines.remove(found)

        collection.document(id.toString())
            .delete()
            .addOnFailureListener { Timber.e(it, "delete failed for id=$id") }

        Timber.d("delete: id=$id")
        return true
    }

    override fun findOne(id: Long): MachineModel? {
        return machines.find { it.id == id }
    }

    override fun setOnChangeListener(listener: (() -> Unit)?) {
        onChange = listener
    }

    // stop listening when the user logs out, otherwise it keeps running in the background
    fun stop() {
        registration.remove()
        Timber.d("Stopped listening to Firestore for user $userId")
    }
}
