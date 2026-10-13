package l05_oop.practice

import l05_oop.practice.shapes.Shape
import l05_oop.practice.shapes.area
import l05_oop.practice.shapes.describe
import l05_oop.practice.shapes.largestFirst
import l05_oop.practice.shapes.perimeter
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ShapesTest {
    @Test
    fun `area and perimeter of every shape`() {
        assertEquals(PI, area(Shape.Circle(1.0)), DELTA)
        assertEquals(2 * PI, perimeter(Shape.Circle(1.0)), DELTA)
        assertEquals(8.0, area(Shape.Rectangle(2.0, 4.0)), DELTA)
        assertEquals(12.0, perimeter(Shape.Rectangle(2.0, 4.0)), DELTA)
        assertEquals(6.0, area(Shape.Triangle(3.0, 4.0, 5.0)), DELTA)
        assertEquals(12.0, perimeter(Shape.Triangle(3.0, 4.0, 5.0)), DELTA)
    }

    @Test
    fun `invalid shapes cannot be created`() {
        assertFailsWith<IllegalArgumentException> { Shape.Circle(0.0) }
        assertFailsWith<IllegalArgumentException> { Shape.Rectangle(-1.0, 2.0) }
        assertFailsWith<IllegalArgumentException> { Shape.Triangle(1.0, 2.0, 10.0) }
        assertFailsWith<IllegalArgumentException>("Flat triangle: 1 + 2 == 3") { Shape.Triangle(1.0, 2.0, 3.0) }
    }

    @Test
    fun `largestFirst sorts by area descending`() {
        val circle = Shape.Circle(1.0)
        val rectangle = Shape.Rectangle(2.0, 4.0)
        val triangle = Shape.Triangle(3.0, 4.0, 5.0)
        assertEquals(listOf(rectangle, triangle, circle), largestFirst(listOf(circle, rectangle, triangle)))
    }

    @Test
    fun `describe rounds to two decimals`() {
        assertEquals("Circle(r=1.0): area 3.14, perimeter 6.28", describe(Shape.Circle(1.0)))
    }

    private companion object {
        const val DELTA = 1e-9
    }
}
