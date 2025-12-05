package org.balefrost.aoc2025.day05

import org.balefrost.aoc2025.readInputLines
import kotlin.math.max

data class Input(val ranges: List<LongRange>, val ids: List<Long>)

fun parseInput(lines: List<String>): Input {
    val br = lines.indexOf("")
    val rangeRe = """(\d+)-(\d+)""".toRegex()
    val ranges = lines.subList(0, br).map {
        val m = rangeRe.matchEntire(it)
        m!!.groupValues[1].toLong()..m.groupValues[2].toLong()
    }
    val ids = lines.subList(br + 1, lines.size).map { it.toLong() }

    return Input(ranges, ids)
}

object Day05Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day05.txt")
        val input = parseInput(lines)
        println(input.ids.count { id ->
            input.ranges.any { r -> id in r }
        })
    }
}

object Day05Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day05.txt")
        val input = parseInput(lines)

        val joinedRanges = sequence {
            val iter = input.ranges.sortedBy { it.first }.iterator()
            if (!iter.hasNext()) {
                return@sequence
            }
            var pendingRange: LongRange = iter.next()
            while (iter.hasNext()) {
                val nextRange = iter.next()
                if (pendingRange.contains(nextRange.first)) {
                    pendingRange = pendingRange.first .. max(pendingRange.last, nextRange.last)
                } else {
                    yield(pendingRange)
                    pendingRange = nextRange
                }
            }
            yield(pendingRange)
        }

        println(joinedRanges.sumOf { it.last - it.first + 1 })
    }
}