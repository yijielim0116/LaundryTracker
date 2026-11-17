package org.setu.laundrytracker.models

/**
 * Whether a machine washes or dries clothes.
 */
enum class MachineType {
    WASHER,
    DRYER
}

/**
 * What a machine is doing right now.
 */
enum class MachineStatus {
    AVAILABLE,
    IN_USE,
    OUT_OF_ORDER
}

// the enum names (WASHER, IN_USE...) look ugly on screen, so these give normal words
// (they used to be in MachineAdapter, moved here so the map can use them too)
fun MachineType.label(): String {
    return when (this) {
        MachineType.WASHER -> "Washer"
        MachineType.DRYER -> "Dryer"
    }
}

fun MachineStatus.label(): String {
    return when (this) {
        MachineStatus.AVAILABLE -> "Available"
        MachineStatus.IN_USE -> "In use"
        MachineStatus.OUT_OF_ORDER -> "Out of order"
    }
}

/**
 * Data class representing a single laundry machine.
 * Kotlin automatically generates toString(), equals(), hashCode(), and copy().
 */
data class MachineModel(
    var id: Long = 0L,
    val name: String = "",
    val location: String = "",
    val type: MachineType = MachineType.WASHER,
    val status: MachineStatus = MachineStatus.AVAILABLE,
    // how long one wash/dry cycle takes in minutes, 60 if I don't pick one
    val cycleMinutes: Int = 60,
    // the machine's photo as a uri string, null if it doesn't have one
    // nullable so the machines saved before photos existed still load from machines.json
    val imageUri: String? = null,
    // where the machine is, picked on the map, null until I set it
    // (nullable for the same reason as imageUri, old machines.json files don't have these)
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    // only machines with both numbers can go on the map
    fun hasLocation(): Boolean {
        return latitude != null && longitude != null
    }
}
