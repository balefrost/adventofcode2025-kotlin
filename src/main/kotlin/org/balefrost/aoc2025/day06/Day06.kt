package org.balefrost.aoc2025.day06

import org.balefrost.aoc2025.readInputLines

data class Problem(val operator: String, val operands: List<Long>)

data class Input(val problems: List<Problem>)

object Day06Part01 {
    fun parseInput(lines: List<String>): Input {
        val r = """[\s\n]+""".toRegex()
        val numbers = lines.dropLast(1).map { line ->
            line.trim().split(r).map { it.toLong() } }
        val operators = lines.last().trim().split(r)
        val numColumns = operators.size
        assert(numbers.all { it.size == numColumns })
        val problems = (0 ..< numColumns).map { col ->
            Problem(operators[col], numbers.map { it[col] })
        }
        return Input(problems)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day06.txt")
        val input = parseInput(lines)
        println(input.problems.sumOf { p ->
            val op = when (p.operator) {
                "+" -> { a: Long, b: Long -> a + b }
                "*" -> { a: Long, b: Long -> a * b }
                else -> error("Invalid operator")
            }
            p.operands.reduce(op)
        })
    }
}

object Day06Part02 {
    fun parseInput(lines: List<String>): Input {
        val spaceColumns = lines.map { line ->
            line.indices.filter { line[it] == ' '}.toSet()
        }.reduce { a, b -> a.intersect(b) }.sorted()
        val ranges = (listOf(-1) + spaceColumns + lines.maxOf { it.length }).zipWithNext { a, b -> a + 1 ..< b }
        val problems = ranges.map { range ->
            val operator = lines.last().substring(range).trim()
            val operands = range.map { c ->
                lines.dropLast(1)
                    .map { l -> l.getOrNull(c) ?: ' ' }
                    .filter { it != ' ' }
                    .map { it.digitToInt().toLong() }
                    .reduce { a, b -> a * 10 + b }
            }
            Problem(operator, operands)
        }
        return Input(problems)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day06.txt")
        val input = parseInput(lines)
        println(input.problems.sumOf { p ->
            val op = when (p.operator) {
                "+" -> { a: Long, b: Long -> a + b }
                "*" -> { a: Long, b: Long -> a * b }
                else -> error("Invalid operator")
            }
            p.operands.reduce(op)
        })
    }
}