package me.mibers

import net.minestom.server.entity.Player
import net.minestom.server.map.framebuffers.DirectFramebuffer
import kotlin.math.*

/*
represents a 128x128 pixel grid where each pixel is stored as a nibble (4 bits, 16 colors).
the grid is stored in a compact ByteArray, with two pixels packed into each byte.
*/
class PixelGrid {
    companion object {
        const val WIDTH = 128
        const val HEIGHT = 128
        const val BYTE_WIDTH = WIDTH / 2 // 64 bytes / row, 2 nibbles in one byte
        private const val NIBBLE_MASK = 0xF
        private const val LOW_NIBBLE_MASK = 0x0F
        private const val HIGH_NIBBLE_MASK = 0xF0
    }

    private val grid = ByteArray(BYTE_WIDTH * HEIGHT) // 8192 bytes for grid
    private val framebuffer = DirectFramebuffer()

    // check if xy is in bounds
    private fun isOutBounds(x: Int, y: Int): Boolean {
        return (x !in 0 until WIDTH || y !in 0 until HEIGHT)
    }

    // calc byte index in 1d array
    private fun getByteIndex(x: Int, y: Int): Int {
        return (y * BYTE_WIDTH) + (x / 2)
    }

    /* high nibble case, clear high nibble and set to new value
    1. clear high nibble using 0x0F (00001111)
    2. shift the value 4 bits to the left; position it as the high nibble
    3. combine with the lower nibble using 'or' */
    private fun setHighNibble(byte: Byte, value: Int): Byte {
        return ((byte.toInt() and LOW_NIBBLE_MASK) or (value shl 4)).toByte()
    }

    /* low nibble case, clear low nibble and set new value,
    1. clear low nibble using 0xF0 (11110000)
    2. combine (or) with the new value */
    private fun setLowNibble(byte: Byte, value: Int): Byte {
        return ((byte.toInt() and HIGH_NIBBLE_MASK) or (value and NIBBLE_MASK)).toByte()
    }

    // >> to get high nibble then mask 0xF
    private fun getHighNibble(byte: Byte): Int {
        return (byte.toInt() shr 4) and NIBBLE_MASK
    }

    // mask 0xF to get low nibble
    private fun getLowNibble(byte: Byte): Int {
        return byte.toInt() and NIBBLE_MASK
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        if (isOutBounds(x, y)) return
        val colorValue = color % 16
        val byteIndex = getByteIndex(x, y)
        grid[byteIndex] = if ((x % 2) == 0) {
            setHighNibble(grid[byteIndex], colorValue)
        } else {
            setLowNibble(grid[byteIndex], colorValue)
        }
    }

    fun getPixel(x: Int, y: Int): Int {
        if (isOutBounds(x, y)) return 0
        val byteIndex = getByteIndex(x, y)
        return if (x % 2 == 0) {
            getHighNibble(grid[byteIndex])
        } else {
            getLowNibble(grid[byteIndex])
        }
    }

    // clears the grid with the nibble value
    fun clear(value: Int) {
        // lower 4 bits only
        val nibble = (value and NIBBLE_MASK)
        // pack (<<) nibble into both halves of the byte, 0x3 -> 0x33,
        val packedValue = ((nibble shl 4) or nibble).toByte()
        grid.fill(packedValue)
    }

    fun line(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        var x1 = x1
        var y1 = y1
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val sx = if (x1 < x2) 1 else -1
        val sy = if (y1 < y2) 1 else -1
        var err = dx - dy

        while (true) {
            setPixel(x1, y1, color)
            if (x1 == x2 && y1 == y2) break
            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x1 += sx
            }
            if (e2 < dx) {
                err += dx
                y1 += sy
            }
        }
    }

    fun circ(cx: Int, cy: Int, r: Int, color: Int) {
        var d = (5 - r * 4) / 4
        var x = 0
        var y = r
        do {
            setPixel(cx + x, cy + y, color)
            setPixel(cx + x, cy - y, color)
            setPixel(cx - x, cy + y, color)
            setPixel(cx - x, cy - y, color)
            setPixel(cx + y, cy + x, color)
            setPixel(cx + y, cy - x, color)
            setPixel(cx - y, cy + x, color)
            setPixel(cx - y, cy - x, color)
            if (d < 0) {
                d += 2 * x + 1
            }
            else {
                d += 2 * (x - y) + 1
                y--
            }
            x++
        }
        while (x <= y)
    }

    fun circFill(cx: Int, cy: Int, r: Int, color: Int) {
        var d = (5 - r * 4) / 4
        var x = 0
        var y = r

        do {
            // Draw horizontal lines to fill the circle
            for (i in cx - x..cx + x) {
                setPixel(i, cy + y, color) // Bottom half
                setPixel(i, cy - y, color) // Top half
            }
            for (i in cx - y..cx + y) {
                setPixel(i, cy + x, color) // Right half
                setPixel(i, cy - x, color) // Left half
            }

            if (d < 0) {
                d += 2 * x + 1
            } else {
                d += 2 * (x - y) + 1
                y--
            }
            x++
        } while (x <= y)
    }

    fun rect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        for (x in x1..x2) {
            setPixel(x, y1, color) // Top edge
            setPixel(x, y2, color) // Bottom edge
        }
        for (y in y1..y2) {
            setPixel(x1, y, color) // Left edge
            setPixel(x2, y, color) // Right edge
        }
    }

    fun rectFill(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        val startX = minOf(x1, x2)
        val endX = maxOf(x1, x2)
        val startY = minOf(y1, y2)
        val endY = maxOf(y1, y2)

        for (y in startY..endY) {
            for (x in startX..endX) {
                setPixel(x, y, color)
            }
        }
    }

    fun print(text: String, x: Int, y: Int, color: Int) {
        var currentX = x
        for (char in text) {
            if (currentX >= WIDTH) break // prevent overflow
            // TODO: implement font system chars will be 4x6,
            setPixel(currentX, y, color)
            currentX++
        }
    }

    fun updateFramebuffer() {
        for (i in 0 until WIDTH) {
            for (j in 0 until HEIGHT) {
                framebuffer.set(i, j, Color.getByte(getPixel(i, j)))
            }
        }
    }
    fun getFramebuffer(): DirectFramebuffer {
        return framebuffer
    }
}

fun sendFramebuffer(player: Player, pixelGrid: PixelGrid) {
    pixelGrid.updateFramebuffer()
    player.sendPacket(pixelGrid.getFramebuffer().preparePacket(1))
}
