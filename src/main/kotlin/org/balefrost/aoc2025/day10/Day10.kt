package org.balefrost.aoc2025.day10

import org.balefrost.aoc2025.readInputLines
import java.util.PriorityQueue
import kotlin.collections.map

data class Input(
    val targetPattern: List<Boolean>,
    val buttons: List<List<Int>>,
    val targetJoltage: List<Int>
)

fun parseInput(lines: List<String>): List<Input> {
    val patternRe = """\[(.*?)]""".toRegex()
    val buttonsRe = """\((.*?)\)""".toRegex()
    val joltageRe = """\{(.*?)}""".toRegex()

    return lines.map { line ->
        val pattern = patternRe.find(line)!!.groups[1]!!.value.map { it == '#' }
        val buttons = buttonsRe.findAll(line).map { buttonSpec ->
            buttonSpec.groupValues[1].split(",").map { it.toInt() }
        }.toList()
        val joltage = joltageRe.find(line)!!.groupValues[1].split(",").map { it.toInt() }
        Input(pattern, buttons, joltage)
    }
}

object Day10Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day10.txt")
//        val lines = """
//            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
//            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
//            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
//        """.trimIndent().lines()

        val allInput = parseInput(lines)

        val costs = allInput.map { input ->
            data class Item(val pattern: List<Boolean>, val cost: Long)

            val onQ = mutableSetOf<List<Boolean>>()
            val q = PriorityQueue<Item>(compareBy { it.cost })
            val initialPattern = List(input.targetPattern.size) { false }
            q.add(Item(initialPattern, 0))
            onQ.add(initialPattern)

            while (!q.isEmpty()) {
                val (pattern, cost) = q.remove()
                if (pattern == input.targetPattern) {
                    return@map cost
                }
                input.buttons.asSequence().forEach { button ->
                    val m = pattern.toMutableList()
                    for (i in button) {
                        m[i] = !m[i]
                    }
                    if (onQ.add(m)) {
                        q.add(Item(m, cost + 1))
                    }
                }
            }
            error("Could not reach target pattern")
        }

        println(costs.sum())
    }
}

object Day10Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day10.txt")
//        val lines = """
//            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
//            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
//            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
//        """.trimIndent().lines()

        val allInput = parseInput(lines)

        val costs = allInput.map { input ->
            println(input)
            val mostIncreases = input.buttons.maxOf { it.size }
            data class Item(val pattern: List<Int>) {
                val isValid get() = input.targetJoltage.zip(pattern) { a, b -> a >= b }.all { it }
                // Can be an under-estimate
                val estimatedRemaining by lazy {
                    val distance = input.targetJoltage.zip(pattern) { a, b -> a - b }.sum()
                    (distance + (mostIncreases - 1)) / mostIncreases
                }

                override fun toString(): String {
                    return "Item(pattern = $pattern, estimatedRemaining=$estimatedRemaining)"
                }
            }

            val maxPosition = input.targetJoltage.indices.maxBy { input.targetJoltage[it] }
            val maxPresses = input.targetJoltage[maxPosition]
            val someButton = input.buttons.filter { it.contains(maxPosition) }.maxBy { it.size }

//            val onQ = mutableSetOf<List<Int>>()
            val bestCosts = mutableMapOf<Item, Int>()
            fun getBestCost(item: Item): Int {
                return bestCosts.getOrDefault(item, Int.MAX_VALUE)
            }
            val q = PriorityQueue<Item>(
                compareBy<Item> {
                    getBestCost(it) + it.estimatedRemaining }.thenBy { it.estimatedRemaining })

            (0..maxPresses).map { presses ->
                Item(List(input.targetJoltage.size) { if (it in someButton) presses else 0 }) to presses
            }.filter { it.first.isValid }.forEach { (item, presses) ->
                bestCosts[item] = presses
                q.add(item)
            }
            repeat(maxPresses) { presses ->
                val item = Item(List(input.targetJoltage.size) { if (it in someButton) presses else 0 })
            }

            while (!q.isEmpty()) {
                val item = q.remove()
                val myCost = getBestCost(item)
                if (item.pattern == input.targetJoltage) {
                    return@map myCost
                }
                if (item.pattern.zip(input.targetJoltage).any { (a, b) -> a > b }) {
                    continue
                }
                input.buttons.asSequence().forEach { button ->
                    val m = item.pattern.toMutableList()
                    for (i in button) {
                        m[i] = m[i] + 1
                    }
                    val nextItem = Item(m)
                    if (myCost + 1 < getBestCost(nextItem)) {
                        q.remove(nextItem)
                        bestCosts[nextItem] = myCost + 1
                        q.add(nextItem)
                    }
                }
            }
            error("Could not reach target pattern")
        }

        println(costs)
        println(costs.sum())
    }
}