package l05_oop.practice.parking

/**
 * =====================================================================
 *  PRACTICE 05.C — Parking lot
 * =====================================================================
 *  GOAL   : Model a real system with enums, sealed vehicle types, a
 *           class that guards its state, and sealed results.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.practice.parking.ParkingLotKt
 *  OUTPUT :
 *           Lot with 4 spots: [SMALL, MEDIUM, MEDIUM, LARGE]
 *           Motorcycle M-1 -> parked in spot 0
 *           Car C-1 -> parked in spot 1
 *           Car C-2 -> parked in spot 2
 *           Car C-3 -> parked in spot 3
 *           Bus B-1 -> no free spot fits
 *           Car C-1 -> already parked in spot 1
 *           Leave C-2 -> left spot 2
 *           Bus B-1 -> no free spot fits
 *           Leave X-9 -> not found
 *           Free spots: 1 of 4
 *  DESIGN : A vehicle fits any spot that is at least its size, and it
 *           gets the SMALLEST free spot that fits (so big spots stay free
 *           for big vehicles). The lot keeps its map private and answers
 *           with sealed results. (mutableMapOf, toList: level 06.)
 *  EXTEND : Charge a fee per hour by vehicle type when it leaves.
 * =====================================================================
 */

enum class SpotSize { SMALL, MEDIUM, LARGE }

sealed interface Vehicle {
    val plate: String
    val size: SpotSize

    data class Motorcycle(override val plate: String) : Vehicle {
        override val size: SpotSize get() = SpotSize.SMALL
    }

    data class Car(override val plate: String) : Vehicle {
        override val size: SpotSize get() = SpotSize.MEDIUM
    }

    data class Bus(override val plate: String) : Vehicle {
        override val size: SpotSize get() = SpotSize.LARGE
    }
}

sealed interface ParkResult {
    data class Parked(val spot: Int) : ParkResult

    data class AlreadyParked(val spot: Int) : ParkResult

    data object NoFittingSpot : ParkResult
}

sealed interface LeaveResult {
    data class Left(val spot: Int, val vehicle: Vehicle) : LeaveResult

    data object NotFound : LeaveResult
}

class ParkingLot(spotSizes: List<SpotSize>) {
    // Copy the input, so the caller can't change our spots later.
    val spots: List<SpotSize> = spotSizes.toList()

    // spot number -> vehicle parked there. Private: only park/leave change it.
    private val occupied = mutableMapOf<Int, Vehicle>()

    val freeSpotCount: Int
        get() = spots.size - occupied.size

    fun park(vehicle: Vehicle): ParkResult {
        val current = spotOf(vehicle.plate)
        if (current != null) return ParkResult.AlreadyParked(current)

        // Try sizes from small to large; the first free spot that fits wins.
        for (size in SpotSize.entries) {
            if (size < vehicle.size) continue // enums compare by declaration order
            for (spot in spots.indices) {
                if (spots[spot] == size && spot !in occupied) {
                    occupied[spot] = vehicle
                    return ParkResult.Parked(spot)
                }
            }
        }
        return ParkResult.NoFittingSpot
    }

    fun leave(plate: String): LeaveResult {
        val spot = spotOf(plate) ?: return LeaveResult.NotFound
        val vehicle = occupied.getValue(spot)
        occupied.remove(spot)
        return LeaveResult.Left(spot, vehicle)
    }

    private fun spotOf(plate: String): Int? {
        for ((spot, vehicle) in occupied) {
            if (vehicle.plate == plate) return spot
        }
        return null
    }
}

fun kind(vehicle: Vehicle): String =
    when (vehicle) {
        is Vehicle.Motorcycle -> "Motorcycle"
        is Vehicle.Car -> "Car"
        is Vehicle.Bus -> "Bus"
    }

fun describePark(vehicle: Vehicle, result: ParkResult): String {
    val outcome =
        when (result) {
            is ParkResult.Parked -> "parked in spot ${result.spot}"
            is ParkResult.AlreadyParked -> "already parked in spot ${result.spot}"
            ParkResult.NoFittingSpot -> "no free spot fits"
        }
    return "${kind(vehicle)} ${vehicle.plate} -> $outcome"
}

fun describeLeave(plate: String, result: LeaveResult): String =
    when (result) {
        is LeaveResult.Left -> "Leave $plate -> left spot ${result.spot}"
        LeaveResult.NotFound -> "Leave $plate -> not found"
    }

fun main() {
    val lot = ParkingLot(listOf(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.MEDIUM, SpotSize.LARGE))
    println("Lot with ${lot.spots.size} spots: ${lot.spots}")

    val bus = Vehicle.Bus("B-1")
    val arrivals =
        listOf(
            Vehicle.Motorcycle("M-1"),
            Vehicle.Car("C-1"),
            Vehicle.Car("C-2"),
            Vehicle.Car("C-3"), // only the LARGE spot is left: a car may use it
            bus,
            Vehicle.Car("C-1"),
        )
    for (vehicle in arrivals) {
        println(describePark(vehicle, lot.park(vehicle)))
    }

    println(describeLeave("C-2", lot.leave("C-2")))
    println(describePark(bus, lot.park(bus))) // a MEDIUM spot is free, but a bus needs LARGE
    println(describeLeave("X-9", lot.leave("X-9")))
    println("Free spots: ${lot.freeSpotCount} of ${lot.spots.size}")
}
