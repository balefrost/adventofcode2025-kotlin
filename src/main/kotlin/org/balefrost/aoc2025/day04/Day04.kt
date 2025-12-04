package org.balefrost.aoc2025.day04

import org.balefrost.aoc2025.makeMutableMapFromLines
import org.balefrost.aoc2025.readInputLines

object Day04Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day04.txt")
        val m = makeMutableMapFromLines(lines, '.')
        println(m.positions.count { pos->
            m[pos] == '@' && pos.dirs8way.count { m[it] == '@' } < 4
        })
    }
}

object Day04Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day04.txt")
        val m = makeMutableMapFromLines(lines, '.')
        var totalRemoved = 0
        while (true) {
            val positions = m.positions.filter { pos->
                m[pos] == '@' && pos.dirs8way.count { m[it] == '@' } < 4
            }.toList()
            if (positions.isEmpty()) break
            totalRemoved += positions.size
            for (pos in positions) {
                m[pos] = '.'
            }
        }
        println(totalRemoved)
    }
}