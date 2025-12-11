package org.balefrost.aoc2025.day11

import org.balefrost.aoc2025.doDynamicProg
import org.balefrost.aoc2025.doMultiDynamicProg
import org.balefrost.aoc2025.readInputLines

object Day11Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day11.txt")

        val connections = lines.associate { line ->
            val (a, b) = line.split(": ")
            val bs = b.split(" ")
            a to bs.toSet()
        }

        println(doDynamicProg<String, Int>("you") { from ->
            if (from == "out") {
                return@doDynamicProg 1
            }
            var numPathsToTarget = 0
            for (next in (connections[from] ?: emptySet())) {
                numPathsToTarget += recur(next)
            }
            return@doDynamicProg numPathsToTarget
        })
    }
}

object Day11Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day11.txt")

        val connections = lines.associate { line ->
            val (a, b) = line.split(": ")
            val bs = b.split(" ")
            a to bs.toSet()
        }

        val stepLists = listOf(
            listOf(
                "svr" to "dac",
                "dac" to "fft",
                "fft" to "out"
            ),
            listOf(
                "svr" to "fft",
                "fft" to "dac",
                "dac" to "out"
            )
        )

        val targets = stepLists.flatten()

        val results = doMultiDynamicProg<Pair<String, String>, Long>(targets) { (from, to) ->
            if (from == to) {
                return@doMultiDynamicProg 1
            }
            var numPathsToTarget = 0L
            for (next in (connections[from] ?: emptySet())) {
                numPathsToTarget += recur(next to to)
            }
            return@doMultiDynamicProg numPathsToTarget
        }

        println(stepLists.sumOf { steps ->
            steps.map(results::getValue).reduce { a, b -> a * b }
        })
    }
}