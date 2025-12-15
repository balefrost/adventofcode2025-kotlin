package org.balefrost.aoc2025.day12

import org.balefrost.aoc2025.readInputLines
import org.balefrost.aoc2025.runRecursive
import kotlin.math.min

interface Pattern {
    val w: Int
    val h: Int
    val xRange: IntRange get() = 0..<w
    val yRange: IntRange get() = 0..<h
    operator fun get(x: Int, y: Int): Boolean
    fun rotateRight(): MutablePattern
    val rotations: Sequence<MutablePattern>
    fun intersects(p: Pattern, x: Int, y: Int): Boolean
    fun visualize(): String
}

class MutablePattern private constructor(
    private val data: IntArray,
    override val w: Int,
    override val h: Int
) : Pattern {
    constructor(w: Int, h: Int) : this(makeArray(w, h), w, h)

    override operator fun get(x: Int, y: Int): Boolean {
        val (byteNumber, bitNumber) = getBitPos(x, y)
        return (data[byteNumber] and (1 shl bitNumber)) != 0
    }

    operator fun set(x: Int, y: Int, value: Boolean) {
        val (byteNumber, bitNumber) = getBitPos(x, y)
        if (value) {
            data[byteNumber] = data[byteNumber] or (1 shl bitNumber);
        } else {
            data[byteNumber] = data[byteNumber] and ((1 shl bitNumber).inv());
        }
    }

    fun copy(): MutablePattern {
        return MutablePattern(data.copyOf(), w, h)
    }

    override fun rotateRight(): MutablePattern {
        val result = MutablePattern(h, w)
        for (x in 0..<w) {
            for (y in 0..<h) {
                result[h - y - 1, x] = this[x, y]
            }
        }
        return result
    }

    override val rotations
        get() = sequence {
            var p = this@MutablePattern
            yield(p)
            repeat(3) {
                p = p.rotateRight()
                yield(p)
            }
        }

    override fun intersects(p: Pattern, x: Int, y: Int): Boolean {
        for (myX in x..<min(w, x + p.w)) {
            for (myY in y..<min(h, y + p.h)) {
                if (this[myX, myY] && p[myX - x, myY - y]) {
                    return true
                }
            }
        }
        return false
    }

    fun merge(p: Pattern, x: Int, y: Int) {
        for (myX in x..<min(w, x + p.w)) {
            for (myY in y..<min(h, y + p.h)) {
                this[myX, myY] = this[myX, myY] || p[myX - x, myY - y]
            }
        }
    }

    override fun visualize(): String {
        return buildString {
            for (y in 0..<h) {
                for (x in 0..<w) {
                    append(if (this@MutablePattern[x, y]) '#' else '.')
                }
                append("\n")
            }
        }
    }

    private fun getBitPos(x: Int, y: Int): Pair<Int, Int> {
        val bitIndex = y * w + x
        val byteNumber = bitIndex / 32
        val bitNumber = bitIndex % 32
        return byteNumber to bitNumber
    }

    companion object {
        fun fromLines(lines: List<String>): MutablePattern {
            val w = lines.maxOf { it.length }
            val h = lines.size
            val result = MutablePattern(w, h)
            for ((y, line) in lines.withIndex()) {
                for ((x, ch) in line.withIndex()) {
                    result[x, y] = ch == '#'
                }
            }
            return result
        }

        private fun makeArray(w: Int, h: Int): IntArray {
            val numBytes = (w * h + 7) / 8
            return IntArray(numBytes)
        }
    }
}

data class Region(val w: Int, val h: Int, val numbers: List<Int>)

object Day12Part01 {
    @JvmStatic
    fun main(args: Array<String>) {
        val lines = readInputLines("inputs/day12.txt")

        val indexRegex = """(\d+):""".toRegex()
        val regionRegex = """(\d+)x(\d+): (.*)""".toRegex()
        val lineIterator = lines.listIterator()
        lateinit var line: String
        val patterns = mutableListOf<MutablePattern>()
        while (lineIterator.hasNext()) {
            line = lineIterator.next()
            val indexMatch = indexRegex.matchEntire(line)
            if (indexMatch == null) {
                break
            }
            val index = indexMatch.groupValues[1].toInt()
            check(index == patterns.size)
            line = lineIterator.next()
            val patternLines = mutableListOf<String>()
            while (line.isNotEmpty()) {
                patternLines.add(line)
                line = lineIterator.next()
            }
            val w = patternLines.maxOf { it.length }
            val pattern = MutablePattern(w, patternLines.size)
            for ((y, line) in patternLines.withIndex()) {
                for ((x, ch) in line.withIndex()) {
                    pattern[x, y] = ch == '#'
                }
            }
            patterns.add(pattern)
        }

        val regions = mutableListOf<Region>()
        while (true) {
            val regionMatch = regionRegex.matchEntire(line)
            checkNotNull(regionMatch)
            val w = regionMatch.groupValues[1].toInt()
            val h = regionMatch.groupValues[2].toInt()
            val numbers = regionMatch.groupValues[3].split(" ").map { it.toInt() }
            regions.add(Region(w, h, numbers))
            if (!lineIterator.hasNext()) {
                break
            }
            line = lineIterator.next()
        }

        fun tryPlaceTiles(tiles: List<Pattern>, w: Int, h: Int): Boolean {
            val p = MutablePattern(w, h)
            return runRecursive {
                suspend fun tryPlaceRemainingTiles(p: MutablePattern, tiles: List<Pattern>): Boolean {
                    if (tiles.isEmpty()) {
                        return true
                    }
                    val t = tiles.first()
                    val ts = tiles.subList(1, tiles.size)
                    for (r in t.rotations) {
                        val xRange = 0..(p.w - r.w)
                        val yRange = 0..(p.h - r.h)
                        for (x in xRange) {
                            for (y in yRange) {
                                if (!p.intersects(r, x, y)) {
                                    val c = p.copy()
                                    c.merge(t, x, y)
                                    if (recur { tryPlaceRemainingTiles(c, ts) }) {
                                        return true
                                    }
                                }
                            }
                        }
                    }

                    return false
                }
                tryPlaceRemainingTiles(p, tiles)
            }
        }

        println(regions.filter { region ->
            val tiles = sequence {
                for (item in region.numbers.withIndex()) {
                    val pattern = patterns[item.index]
                    repeat(item.value) {
                        yield(pattern)
                    }
                }
            }.toList()

            val availableArea = region.w * region.h
            val requiredArea = tiles.sumOf { tile ->
                var count = 0
                for (y in tile.yRange) {
                    for (x in tile.xRange) {
                        if (tile[x, y]) {
                            ++count
                        }
                    }
                }
                count
            }

            if (availableArea < requiredArea) {
                return@filter false
            }

            runRecursive {
                tryPlaceTiles(tiles, region.w, region.h)
            }
        }.size)
    }
}

object Day12Part02 {
    @JvmStatic
    fun main(args: Array<String>) {
        println("https://www.youtube.com/watch?v=DgMXBUJFZOA")
    }
}