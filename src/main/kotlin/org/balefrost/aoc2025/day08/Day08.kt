package org.balefrost.aoc2025.day08

import org.balefrost.aoc2025.XYZ
import org.balefrost.aoc2025.readInputLines
import kotlin.collections.map
import kotlin.collections.set

fun parseInput(lines: List<String>): List<XYZ> {
    return lines.map { line ->
        val (x, y, z) = line.split(",").map { it.toLong() }
        XYZ(x, y, z)
    }
}

fun computeClosest(positions: List<XYZ>): List<Pair<XYZ, XYZ>> {
    return positions.indices.flatMap { a->
        (a + 1 .. positions.lastIndex).map { b ->
            val aa = positions[a]
            val bb = positions[b]
            (aa to bb) to aa.distanceTo(bb)
        }
    }.sortedBy { it.second }.map { it.first }
}

fun mergeComponents(posToComponent: MutableMap<XYZ, MutableSet<XYZ>>, a: XYZ, b: XYZ): MutableSet<XYZ> {
    val componentA = checkNotNull(posToComponent[a])
    val componentB = checkNotNull(posToComponent[b])
    if (componentA == componentB) {
        return componentA
    }
    componentA.addAll(componentB)
    for (pos in componentB) {
        posToComponent[pos] = componentA
    }
    return componentA
}

object Day08Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day08.txt")
        val input = parseInput(lines)
        val toProcess = computeClosest(input)

        val posToComponent = input.associateWithTo(mutableMapOf()) { mutableSetOf(it) }
        for (pair in toProcess.take(1000)) {
            val (a, b) = pair
            mergeComponents(posToComponent, a, b)
        }

        println(posToComponent.values.distinct().map { it.size }.sortedDescending().take(3).reduce { a, b -> a * b })
    }
}

object Day08Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day08.txt")
        val input = parseInput(lines)
        val toProcess = computeClosest(input)

        val posToComponent = input.associateWithTo(mutableMapOf()) { mutableSetOf(it) }
        for (pair in toProcess) {
            val (a, b) = pair
            val mergedComponent = mergeComponents(posToComponent, a, b)
            if (mergedComponent.size == input.size) {
                println(a.x * b.x)
                break
            }
        }
    }
}