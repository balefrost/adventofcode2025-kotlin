package org.balefrost.aoc2025

import java.io.InputStreamReader
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

fun readInputFile(filename: String) =
    InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(filename)!!).use { it.readText() }

fun readInputLines(filename: String): List<String> {
    val allLines = readInputFile(filename).lines()
    val lastRelevantIndex = (allLines.lastIndex downTo 0).firstOrNull { allLines[it].isNotBlank() }
    if (lastRelevantIndex == null) {
        return emptyList()
    }
    return allLines.subList(0, lastRelevantIndex + 1)
}

fun <T> sortPartiallyOrdered(items: Iterable<T>, getDeps: (T) -> Iterable<T>): Iterable<T> {
    return sequence {
        val emitted = mutableSetOf<T>()
        for (firstItem in items) {
            val onStack = mutableSetOf(firstItem)

            // invariant: contains 0 or more deques, each of which contains at least 1 item.
            val stack = mutableListOf(ArrayDeque<T>().also { it.add(firstItem) })

            fun removeTopOfStack(): T {
                val removedItem = stack.last().removeFirst()
                onStack -= removedItem
                if (stack.last().isEmpty()) {
                    stack.removeLast()
                } else {
                    onStack += stack.last().first()
                }
                return removedItem
            }

            fun addToTopOfStack(items: Iterable<T>): Boolean {
                val deps = ArrayDeque<T>()
                for (item in items) {
                    if (item in emitted) {
                        continue
                    }
                    deps.addLast(item)
                }

                if (deps.isNotEmpty()) {
                    val firstDep = deps.first()
                    if (!onStack.add(firstDep)) {
                        val cycleItems =
                            listOf(firstDep) + stack.asReversed().map { it[0] }.takeWhile { it != firstDep } + listOf(
                                firstDep
                            )
                        throw IllegalArgumentException(
                            "Dependency cycle ${
                                cycleItems.asReversed().joinToString(" -> ")
                            }"
                        )
                    }
                    stack.add(deps)
                    return true
                }

                return false
            }

            while (stack.isNotEmpty()) {
                val item = stack.last().first()
                if (emitted.contains(item)) {
                    removeTopOfStack()
                    continue
                }
                if (!addToTopOfStack(getDeps(item))) {
                    removeTopOfStack()
                    emitted += item
                    yield(item)
                }
            }
        }
    }.asIterable()
}

fun <T> binarySearch(items: List<T>, comparison: (T) -> Int): Int {
    var low = 0
    var high = items.size
    while (low < high) {
        val mid = low + (high - low) / 2
        val midItem = items[mid]
        val comp = comparison(midItem)
        if (comp < 0) {
            high = mid
        } else if (comp > 0) {
            low = mid + 1
        } else {
            return mid
        }
    }
    return low.inv()
}

fun <T> cartesianProduct(items: List<List<T>>): Sequence<List<T>> {
    if (items.isEmpty()) {
        return sequenceOf(emptyList())
    }

    return sequence {
        for (item in items.first()) {
            for (tail in cartesianProduct(items.subList(1, items.size))) {
                yield(listOf(item) + tail)
            }
        }
    }
}

interface EightWay<T> {
    val n: T
    val ne: T
    val e: T
    val se: T
    val s: T
    val sw: T
    val w: T
    val nw: T
}

/**
 * Left-handed XY cartesian point
 */
data class XY(val x: Int, val y: Int) {
    operator fun minus(other: XY): XY = XY(x - other.x, y - other.y)
    operator fun plus(other: XY): XY = XY(x + other.x, y + other.y)
    operator fun unaryMinus() = XY(-x, -y)
    operator fun div(other: XY): XY = XY(x / other.x, y/ other.y)
    val sign: XY get() = XY(x.sign, y.sign)

    fun turnLeft(): XY {
        return XY(y, -x)
    }

    fun turnRight(): XY {
        return XY(-y, x)
    }

    val adjacent
        get() = sequenceOf(
            this + XY(1, 0),
            this + XY(0, 1),
            this + XY(-1, 0),
            this + XY(0, -1)
        )

    fun allWithinDistance(distance: Int): Sequence<XY> {
        return sequence {
            for (yy in y - distance..y + distance) {
                val span = distance - (yy - y).absoluteValue
                for (xx in x - span..x + span) {
                    yield(XY(xx, yy))
                }
            }
        }
    }

    fun manhattanDistanceTo(other: XY): Int {
        return abs(x - other.x) + abs(y - other.y)
    }

    inner class Dirs8Way : EightWay<XY>, Iterable<XY> {
        override val n: XY get() = this@XY + XY(0, -1)
        override val ne: XY get() = this@XY + XY(1, -1)
        override val e: XY get() = this@XY + XY(1, 0)
        override val se: XY get() = this@XY + XY(1, 1)
        override val s: XY get() = this@XY + XY(0, 1)
        override val sw: XY get() = this@XY + XY(-1, 1)
        override val w: XY get() = this@XY + XY(-1, 0)
        override val nw: XY get() = this@XY + XY(-1, -1)

        fun <T> map(fn: (XY) -> T): EightWay<T> {
            return object : EightWay<T> {
                override val n: T = fn(this@Dirs8Way.n)
                override val ne: T = fn(this@Dirs8Way.ne)
                override val e: T = fn(this@Dirs8Way.e)
                override val se: T = fn(this@Dirs8Way.se)
                override val s: T = fn(this@Dirs8Way.s)
                override val sw: T = fn(this@Dirs8Way.sw)
                override val w: T = fn(this@Dirs8Way.w)
                override val nw: T = fn(this@Dirs8Way.nw)
            }
        }

        override fun iterator(): Iterator<XY> = iterator {
            yield(n)
            yield(ne)
            yield(e)
            yield(se)
            yield(s)
            yield(sw)
            yield(w)
            yield(nw)
        }
    }

    val dirs8way = Dirs8Way()
}

/**
 * Left-handed XY cartesian point
 */
data class LongXY(val x: Long, val y: Long) {
    operator fun minus(other: LongXY) = LongXY(x - other.x, y - other.y)
    operator fun plus(other: LongXY) = LongXY(x + other.x, y + other.y)
    operator fun times(multiplier: Long) = LongXY(x * multiplier, y * multiplier)
    operator fun unaryMinus() = LongXY(-x, -y)
    operator fun div(divisor: Long) = LongXY(x / divisor, y / divisor)
    operator fun rem(divisor: LongXY) = LongXY(x % divisor.x, y % divisor.y)
}

data class WH(val w: Int, val h: Int)

class MutableMap2DImpl(data: Iterable<Iterable<Char>>, val oobChar: Char?) : MutableMap2D {
    private val data: List<MutableList<Char>> = data.map { it.toMutableList() }
    override val dims: WH

    init {
        check(this.data.isNotEmpty() && this.data.all { it.size == this.data[0].size })
        this.dims = WH(this.data[0].size, this.data.size)
    }

    override fun contains(pos: XY): Boolean {
        return pos.x in 0..<dims.w && pos.y in 0..<dims.h
    }

    override fun get(pos: XY): Char {
        return when {
            pos in this -> data[pos.y][pos.x]
            oobChar != null -> return oobChar
            else -> throw IndexOutOfBoundsException("$pos outside $dims")
        }
    }

    override val positions: Sequence<XY>
        get() = sequence {
            for (y in 0..<dims.h) {
                for (x in 0..<dims.w) {
                    yield(XY(x, y))
                }
            }
        }

    override fun toMutableMap2D(): MutableMap2D {
        return MutableMap2DImpl(data, oobChar)
    }

    override fun set(pos: XY, value: Char) {
        data[pos.y][pos.x] = value
    }

    override fun toString(): String {
        return " " + (0..<dims.w).joinToString("") { (it % 10).toString() } + "\n" +
                data.withIndex().map { (index, chars) -> (index % 10).toString() + chars.joinToString("") }
                    .joinToString("\n")
    }
}

interface Map2D {
    operator fun contains(pos: XY): Boolean

    operator fun get(pos: XY): Char

    val dims: WH

    val positions: Sequence<XY>

    fun toMutableMap2D(): MutableMap2D
}

interface MutableMap2D : Map2D {
    operator fun set(pos: XY, value: Char)
}

fun makeMutableMapFromLines(lines: List<String>, oobChar: Char? = null): MutableMap2D {
    check(lines.isNotEmpty() && lines.all { it.length == lines[0].length })
    return MutableMap2DImpl(lines.map { it.toList() }, oobChar)
}

fun makeMutableMap(w: Int, h: Int, backgroundChar: Char, oobChar: Char? = null): MutableMap2DImpl {
    return MutableMap2DImpl((0..<h).map { (0..<w).map { backgroundChar } }, oobChar)
}

class StringBased2DMap(val lines: List<String>, val oobChar: Char? = null) {
    operator fun contains(pos: XY): Boolean = pos.y in lines.indices && pos.x in lines[pos.y].indices

    operator fun get(pos: XY): Char {
        if (pos !in this) {
            if (oobChar != null) {
                return oobChar
            }
            throw IndexOutOfBoundsException("$pos")
        }
        return lines[pos.y][pos.x]
    }

    val dims get() = WH(lines[0].length, lines.size)

    val positions
        get() = sequence {
            for (y in lines.indices) {
                for (x in lines[y].indices) {
                    yield(XY(x, y))
                }
            }
        }
}

val <T> List<T>.permutations: Sequence<List<T>>
    get() {
        if (isEmpty()) {
            return sequenceOf(emptyList())
        }
        return sequence {
            for (i in this@permutations.indices) {
                val first = this@permutations[i]
                val rest = this@permutations.subList(0, i) + this@permutations.subList(i + 1, this@permutations.size)
                for (r in rest.permutations) {
                    yield(listOf(first) + r)
                }
            }
        }
    }