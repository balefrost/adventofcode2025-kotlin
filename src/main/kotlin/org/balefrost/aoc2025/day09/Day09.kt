package org.balefrost.aoc2025.day09

import org.balefrost.aoc2025.Rect2D
import org.balefrost.aoc2025.XY
import org.balefrost.aoc2025.chunkBy
import org.balefrost.aoc2025.contains
import org.balefrost.aoc2025.overlaps
import org.balefrost.aoc2025.readInputLines

object Day09Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day09.txt")
        val positions = lines.map { line ->
            val (x, y) = line.split(",").map { it.toInt() }
            XY(x, y)
        }
        println(positions.maxOf { a ->
            positions.filter { it != a }.maxOf { b ->
                val (w, h) = (b - a).abs()
                (w.toLong() + 1) * (h + 1)
            }
        })
    }
}

object Day09Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day09.txt")
        val positions = lines.map { line ->
            val (x, y) = line.split(",").map { it.toInt() }
            XY(x, y)
        }

        val segments = (positions.asSequence() + positions[0]).zipWithNext { a, b -> a to b }.toList()
        val horizontalSegments = segments.filter { (a, b) -> a.y == b.y }.sortedBy { (a, _) -> a.y }

        val xPositions = positions.map { it.x }.distinct().sorted()
        val xRanges =
            (listOf(xPositions[0]..<xPositions[1]) +
                    xPositions.subList(1, xPositions.size - 1).zipWithNext { a, b -> a + 1..b - 1 } +
                    listOf(xPositions[xPositions.size - 2] + 1..xPositions.last()) +
                    xPositions.subList(1, xPositions.size - 1).map { it..it }).sortedBy { it.first }

        val xRangeToYRanges = xRanges.map { span ->
            val yRanges = horizontalSegments.asSequence().filter { (a, b) ->
                (a.x..b.x) overlaps span
            }.chunkBy { (a, b) -> (b - a).x < 0 }.chunked(2) { (enterSegs, exitSegs) ->
                enterSegs.first().first.y..exitSegs.last().first.y
            }.toList()
            check(yRanges.isNotEmpty())
            span to yRanges
        }

        val candidateRects = positions.flatMap { a ->
            positions.filter { it != a }.map { b ->
                Rect2D.fromCorners(a, b)
            }
        }.distinct().sortedByDescending { it.area }

        println(candidateRects.first { r ->
            xRangeToYRanges
                .filter { (xRange) ->
                    xRange overlaps r.xRange
                }
                .all { (_, yRanges) ->
                    yRanges.any {
                        r.yRange in it
                    }
                }
        }.area)
    }
}