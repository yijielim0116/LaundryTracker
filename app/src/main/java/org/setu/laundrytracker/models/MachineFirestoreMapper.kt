package org.setu.laundrytracker.models

/**
 * Turns a machine into the fields of a Firestore document and back again.
 */
// I do it by hand instead of Firestore's toObject() because my MachineModel uses val
// (Firestore can only fill in var / setters), and this way it's plain Kotlin I can unit test

fun MachineModel.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "location" to location,
        // enums saved as their names, e.g. "WASHER", "IN_USE"
        "type" to type.name,
        "status" to status.name,
        "cycleMinutes" to cycleMinutes,
        "imageUri" to imageUri,
        "latitude" to latitude,
        "longitude" to longitude
    )
}

// Firestore gives numbers back as Long / Double, so I convert them myself
// anything missing or broken falls back to the same defaults as MachineModel
fun machineFromFirestore(data: Map<String, Any?>): MachineModel {
    return MachineModel(
        id = (data["id"] as? Number)?.toLong() ?: 0L,
        name = data["name"] as? String ?: "",
        location = data["location"] as? String ?: "",
        type = enumOrDefault(data["type"], MachineType.WASHER),
        status = enumOrDefault(data["status"], MachineStatus.AVAILABLE),
        cycleMinutes = (data["cycleMinutes"] as? Number)?.toInt() ?: 60,
        imageUri = data["imageUri"] as? String,
        latitude = (data["latitude"] as? Number)?.toDouble(),
        longitude = (data["longitude"] as? Number)?.toDouble()
    )
}

// a name that isn't one of the enum values (or no name at all) gives the default instead of crashing
private inline fun <reified T : Enum<T>> enumOrDefault(value: Any?, default: T): T {
    val name = value as? String ?: return default
    return enumValues<T>().firstOrNull { it.name == name } ?: default
}
