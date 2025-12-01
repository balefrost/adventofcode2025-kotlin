package org.balefrost.aoc2025.day01

import org.balefrost.aoc2025.readInputLines

fun parseInput(lines: Iterable<String>): List<Int> {
    return lines.map { l ->
        val dir = l[0]
        val amount = l.substring(1).toInt()
        if (dir == 'R') amount else -amount
    }
}

object Day01Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day01.txt")
        val moves = parseInput(lines)
        val positions = moves.scan(50) { dial, move ->
            ((dial + move) % 100 + 100) % 100
        }
        println(positions.count { it == 0 })
    }
}

object Day01Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day01.txt")
        val moves = parseInput(lines)
        val positions = sequence {
            var dial = 50
            for (move in moves) {
                val end = ((dial + move) % 100 + 100) % 100
                assert(end in 0 .. 99)
                val crosses = if (move > 0) {
                    (dial + move) / 100
                } else {
                    val startOnMirroredDial = (100 - dial) % 100
                    (startOnMirroredDial - move) / 100
                }
                yield(crosses)
                dial = end
            }
        }
        println(positions.sumOf { it })
    }
}