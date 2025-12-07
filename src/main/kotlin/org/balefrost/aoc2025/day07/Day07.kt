package org.balefrost.aoc2025.day07

import org.balefrost.aoc2025.XY
import org.balefrost.aoc2025.doDynamicProg
import org.balefrost.aoc2025.makeMutableGridFromLines
import org.balefrost.aoc2025.readInputLines

object Day07Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day07.txt")
        val grid = makeMutableGridFromLines(lines, '.')
        val start = grid.indexesOf('S').first()
        var frontier = setOf(start.x)

        var numSplits = 0
        for (y in start.y..<grid.dims.h - 1) {
            frontier = frontier.flatMap { x ->
                if (grid[XY(x, y + 1)] == '^') {
                    ++numSplits
                    listOf(x - 1, x + 1)
                } else {
                    listOf(x)
                }
            }.toSet()
        }

        println(numSplits)
    }
}

object Day07Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day07.txt")
        val grid = makeMutableGridFromLines(lines, '.')
        val start = grid.indexesOf('S').first()
        println(doDynamicProg<XY, Long>(start) { pos ->
            if (pos.y == grid.dims.h) {
                return@doDynamicProg 1L
            }
            if (grid[pos + XY(0, 1)] == '^') {
                recur(pos + XY(-1, 1)) + recur(pos + XY(1, 1))
            } else {
                recur(pos + XY(0, 1))
            }
        })
    }
}