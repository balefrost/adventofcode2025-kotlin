package org.balefrost.aoc2025.day02

import org.balefrost.aoc2025.readInputFile

fun parseInput(line: String): List<LongRange> {
    val re = """(\d+)-(\d+)""".toRegex()
    return re.findAll(line).map { r ->
        (r.groups[1]!!.value.toLong())..(r.groups[2]!!.value.toLong())
    }.toList()
}

fun isRepatedPair(s: String): Boolean {
    return s.length % 2 == 0 && s.subSequence(0, s.length / 2) == s.subSequence(s.length / 2, s.length)
}

object Day02Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day02.txt")
        val ranges = parseInput(input)
        println(ranges.sumOf { r ->
            r.filter { n ->
                isRepatedPair(n.toString())
            }.sum()
        })
    }
}

fun isRepeatedString(s: String): Boolean {
   return  (2..s.length)
        .filter { repetitions -> s.length % repetitions == 0 }
        .any { repetitions ->
            val repetitionLength = s.length / repetitions
            val expected = s.subSequence(0, repetitionLength)
            (1 until repetitions).all { r ->
                s.subSequence(r * repetitionLength, r * repetitionLength + repetitionLength) == expected
            }
        }
}

object Day02Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val input = readInputFile("inputs/day02.txt")
        val ranges = parseInput(input)
        println(ranges.sumOf { r ->
            r.filter { n ->
                isRepeatedString(n.toString())
            }.sum()
        })
    }
}