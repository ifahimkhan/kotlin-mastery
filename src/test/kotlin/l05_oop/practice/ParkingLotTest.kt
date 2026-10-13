package l05_oop.practice

import l05_oop.practice.parking.LeaveResult
import l05_oop.practice.parking.ParkResult
import l05_oop.practice.parking.ParkingLot
import l05_oop.practice.parking.SpotSize
import l05_oop.practice.parking.Vehicle
import kotlin.test.Test
import kotlin.test.assertEquals

class ParkingLotTest {
    private fun smallMediumLarge() = ParkingLot(listOf(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE))

    @Test
    fun `vehicles get the smallest spot that fits`() {
        val lot = smallMediumLarge()
        assertEquals(ParkResult.Parked(1), lot.park(Vehicle.Car("C-1")))
        assertEquals(ParkResult.Parked(0), lot.park(Vehicle.Motorcycle("M-1")))
        assertEquals(ParkResult.Parked(2), lot.park(Vehicle.Bus("B-1")))
        assertEquals(0, lot.freeSpotCount)
    }

    @Test
    fun `a smaller vehicle may use a bigger spot but not the other way round`() {
        val lot = ParkingLot(listOf(SpotSize.SMALL, SpotSize.LARGE))
        assertEquals(ParkResult.Parked(1), lot.park(Vehicle.Car("C-1")))
        assertEquals(ParkResult.NoFittingSpot, lot.park(Vehicle.Bus("B-1")))
        assertEquals(ParkResult.NoFittingSpot, lot.park(Vehicle.Car("C-2")), "A car does not fit a SMALL spot")
    }

    @Test
    fun `the same plate cannot park twice`() {
        val lot = smallMediumLarge()
        lot.park(Vehicle.Car("C-1"))
        assertEquals(ParkResult.AlreadyParked(1), lot.park(Vehicle.Car("C-1")))
        assertEquals(2, lot.freeSpotCount)
    }

    @Test
    fun `leaving frees the spot`() {
        val lot = smallMediumLarge()
        val car = Vehicle.Car("C-1")
        lot.park(car)
        assertEquals(LeaveResult.Left(1, car), lot.leave("C-1"))
        assertEquals(LeaveResult.NotFound, lot.leave("C-1"))
        assertEquals(3, lot.freeSpotCount)
    }

    @Test
    fun `changing the input list later does not change the lot`() {
        val sizes = mutableListOf(SpotSize.SMALL)
        val lot = ParkingLot(sizes)
        sizes.add(SpotSize.LARGE)
        assertEquals(1, lot.spots.size)
    }
}
