package org.balefrost.aoc2025.day03

import org.balefrost.aoc2025.readInputLines
import kotlin.math.pow

fun maxJoltage(digits: List<Int>, remaining: Int): Long {
    fun helper(start: Int, remaining: Int): Long {
        if (remaining == 0) { return 0L }
        check(digits.size - start >= remaining)
        for( d in 9 downTo 0) {
            val i = start + digits.subList(start, digits.size).indexOf(d)
            if (i >= start && i <= digits.size - remaining) {
                val y = helper(i + 1, remaining - 1)
                return 10.0.pow((remaining - 1).toDouble()).toLong() * d + y
            }
        }
        error("Could not find any digits in digits")
    }

    return helper(0, remaining)
}

object Day03Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day03.txt")
        println(lines.sumOf { maxJoltage(it.map { it.digitToInt() }, 2) })
    }
}

object Day03Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day03.txt")
        println(lines.sumOf { maxJoltage(it.map { it.digitToInt() }, 12) })
    }
}