package org.balefrost.aoc2025.day09

import org.balefrost.aoc2025.XY
import org.balefrost.aoc2025.readInputLines
import kotlin.math.max

object GenerateSvg {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day09.txt")

        val positions = lines.map { line ->
            val (x, y) = line.split(",").map { it.toInt() }
            XY(x, y)
        }

        val x1 = positions.minOf { it.x }
        val y1 = positions.minOf { it.y }
        val w = positions.maxOf { it.x } - x1 + 1
        val h = positions.maxOf { it.y } - y1 + 1
        val strokeWidth = max(w, h) / 500

        val segments = (positions.asSequence() + positions[0]).zipWithNext { a, b -> a to b }.toList()

        val svgLines = segments.mapIndexed { idx, segment ->
            val (a, b) = segment
            val fract = 0.75 - 0.75 * (idx.toDouble() / segments.size)
            val component = "${fract * 100}%"
            val color = "rgb($component, $component, $component)"
            """<line x1="${a.x}" y1="${a.y}" x2="${b.x}" y2="${b.y}" stroke="$color" stroke-width="$strokeWidth"/>"""
        }

        println(
            """
            <!doctype html>
            <html>
            <body>
            <svg viewBox="${x1 - strokeWidth / 2} ${y1 - strokeWidth / 2} ${w + strokeWidth} ${h + strokeWidth}">
                ${svgLines.joinToString(" ")}
            </svg>
            </body>
            </html>""".trimIndent()
        )
    }
}