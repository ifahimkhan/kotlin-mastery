package l05_oop.practice.shapes

import kotlin.math.PI
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * =====================================================================
 *  PRACTICE 05.B — Shapes with a sealed hierarchy
 * =====================================================================
 *  GOAL   : Model a closed family of shapes, validate them when they are
 *           created, and compute with exhaustive `when` expressions.
 *  RUN    : ./gradlew -q run -Plesson=l05_oop.practice.shapes.ShapesKt
 *  OUTPUT :
 *           Circle(r=1.0): area 3.14, perimeter 6.28
 *           Rectangle(2.0 x 4.0): area 8.0, perimeter 12.0
 *           Triangle(3.0, 4.0, 5.0): area 6.0, perimeter 12.0
 *           Largest first: Rectangle, Triangle, Circle
 *           Total area: 17.14
 *           Triangle(1.0, 2.0, 10.0) -> rejected: Sides 1.0, 2.0, 10.0 do not form a triangle
 *  DESIGN : Each shape checks its own rules in `init`, so an invalid shape
 *           can never exist. area/perimeter/describe are plain functions
 *           with `when`: adding a new shape makes the compiler list every
 *           place to update. (sortedByDescending, sumOf: lambdas, level 07.)
 *  EXTEND : Add a Square, then a `scale(shape, factor)` that returns a new shape.
 * =====================================================================
 */

private const val HUNDREDTHS = 100.0
private const val TWO = 2.0

sealed interface Shape {
    data class Circle(val radius: Double) : Shape {
        init {
            require(radius > 0) { "Radius must be positive, was $radius" }
        }
    }

    data class Rectangle(val width: Double, val height: Double) : Shape {
        init {
            require(width > 0 && height > 0) { "Sides must be positive, was $width x $height" }
        }
    }

    data class Triangle(val a: Double, val b: Double, val c: Double) : Shape {
        init {
            require(a > 0 && b > 0 && c > 0) { "Sides must be positive" }
            // Triangle inequality: any two sides together are longer than the third.
            require(a + b > c && a + c > b && b + c > a) { "Sides $a, $b, $c do not form a triangle" }
        }
    }
}

fun area(shape: Shape): Double =
    when (shape) {
        is Shape.Circle -> PI * shape.radius * shape.radius
        is Shape.Rectangle -> shape.width * shape.height
        is Shape.Triangle -> {
            // Heron's formula: area from the three sides.
            val s = perimeter(shape) / TWO
            sqrt(s * (s - shape.a) * (s - shape.b) * (s - shape.c))
        }
    }

fun perimeter(shape: Shape): Double =
    when (shape) {
        is Shape.Circle -> TWO * PI * shape.radius
        is Shape.Rectangle -> TWO * (shape.width + shape.height)
        is Shape.Triangle -> shape.a + shape.b + shape.c
    }

fun name(shape: Shape): String =
    when (shape) {
        is Shape.Circle -> "Circle"
        is Shape.Rectangle -> "Rectangle"
        is Shape.Triangle -> "Triangle"
    }

fun label(shape: Shape): String =
    when (shape) {
        is Shape.Circle -> "Circle(r=${shape.radius})"
        is Shape.Rectangle -> "Rectangle(${shape.width} x ${shape.height})"
        is Shape.Triangle -> "Triangle(${shape.a}, ${shape.b}, ${shape.c})"
    }

fun roundTo2Decimals(value: Double): Double = (value * HUNDREDTHS).roundToLong() / HUNDREDTHS

fun describe(shape: Shape): String =
    "${label(shape)}: area ${roundTo2Decimals(area(shape))}, perimeter ${roundTo2Decimals(perimeter(shape))}"

fun largestFirst(shapes: List<Shape>): List<Shape> = shapes.sortedByDescending { area(it) }

fun totalArea(shapes: List<Shape>): Double = shapes.sumOf { area(it) }

fun main() {
    val shapes =
        listOf(
            Shape.Circle(radius = 1.0),
            Shape.Rectangle(width = 2.0, height = 4.0),
            Shape.Triangle(3.0, 4.0, 5.0),
        )
    for (shape in shapes) {
        println(describe(shape))
    }
    println("Largest first: ${largestFirst(shapes).joinToString { name(it) }}")
    println("Total area: ${roundTo2Decimals(totalArea(shapes))}")

    // An invalid shape is rejected at construction (try/catch: later level).
    try {
        Shape.Triangle(1.0, 2.0, 10.0)
    } catch (e: IllegalArgumentException) {
        println("Triangle(1.0, 2.0, 10.0) -> rejected: ${e.message}")
    }
}
