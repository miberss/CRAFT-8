package me.mibers

import net.minestom.server.entity.Player
import net.minestom.server.map.framebuffers.DirectFramebuffer
import kotlin.math.*

class PixelGrid {
    companion object {
        const val WIDTH = 128
        const val HEIGHT = 128
        const val BYTE_WIDTH = WIDTH / 2
        private const val NIBBLE_MASK = 0xF
        private val COLOR_CACHE = Array(16) { Color.getByte(it) }
    }
    private val fontRenderer = FontRenderer()
    private val grid = ByteArray(BYTE_WIDTH * HEIGHT)
    private val framebuffer = DirectFramebuffer()

    fun setPixel(x: Int, y: Int, color: Int) {
        if (x !in 0 until WIDTH || y !in 0 until HEIGHT) return
        val byteIndex = y * BYTE_WIDTH + (x shr 1)
        val current = grid[byteIndex].toInt()
        grid[byteIndex] = if (x and 1 == 0) {
            (current and 0x0F or ((color and NIBBLE_MASK) shl 4)).toByte()
        } else {
            (current and 0xF0 or (color and NIBBLE_MASK)).toByte()
        }
    }

    fun getPixel(x: Int, y: Int): Int {
        if (x !in 0 until WIDTH || y !in 0 until HEIGHT) return 0
        val byte = grid[y * BYTE_WIDTH + (x shr 1)].toInt()
        return if (x and 1 == 0) (byte shr 4) and NIBBLE_MASK else byte and NIBBLE_MASK
    }

    fun clear(value: Int) {
        val nibble = (value and NIBBLE_MASK)
        grid.fill(((nibble shl 4) or nibble).toByte())
    }

    fun line(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        var cx = x1
        var cy = y1
        val dx = abs(x2 - cx)
        val dy = -abs(y2 - cy)
        val sx = if (cx < x2) 1 else -1
        val sy = if (cy < y2) 1 else -1
        var err = dx + dy
        val c = color and NIBBLE_MASK

        while (true) {
            if (cx in 0 until WIDTH && cy in 0 until HEIGHT) {
                setPixel(cx, cy, c)
            }

            if (cx == x2 && cy == y2) break
            val e2 = 2 * err
            if (e2 >= dy) {
                err += dy
                cx += sx
            }
            if (e2 <= dx) {
                err += dx
                cy += sy
            }
        }
    }

    fun circ(cx: Int, cy: Int, r: Int, color: Int) {
        var x = 0
        var y = r
        var d = 3 - 2 * r
        val c = color and NIBBLE_MASK

        while (y >= x) {
            setPixel(cx + x, cy + y, c)
            setPixel(cx - x, cy + y, c)
            setPixel(cx + x, cy - y, c)
            setPixel(cx - x, cy - y, c)
            setPixel(cx + y, cy + x, c)
            setPixel(cx - y, cy + x, c)
            setPixel(cx + y, cy - x, c)
            setPixel(cx - y, cy - x, c)

            if (d < 0) {
                d += 4 * x + 6
            } else {
                d += 4 * (x - y) + 10
                y--
            }
            x++
        }
    }

    fun circFill(cx: Int, cy: Int, r: Int, color: Int) {
        var x = 0
        var y = r
        var d = 3 - 2 * r
        val c = color and NIBBLE_MASK

        while (x <= y) {
            for (i in cx - y..cx + y) {
                setPixel(i, cy + x, c)
                setPixel(i, cy - x, c)
            }
            for (i in cx - x..cx + x) {
                setPixel(i, cy + y, c)
                setPixel(i, cy - y, c)
            }
            if (d < 0) {
                d += 4 * x + 6
            } else {
                d += 4 * (x - y) + 10
                y--
            }
            x++
        }
    }

    fun rect(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        val startX = max(0, min(x1, x2))
        val endX = min(WIDTH - 1, max(x1, x2))
        val startY = max(0, min(y1, y2))
        val endY = min(HEIGHT - 1, max(y1, y2))
        val c = color and NIBBLE_MASK

        // horizontal edges
        for (x in startX..endX) {
            setPixel(x, startY, c)
            setPixel(x, endY, c)
        }

        // vertical edges (avoid overlapping corners)
        for (y in startY + 1 until endY) {
            setPixel(startX, y, c)
            setPixel(endX, y, c)
        }
    }

    fun rectFill(x1: Int, y1: Int, x2: Int, y2: Int, color: Int) {
        val startX = max(0, min(x1, x2))
        val endX = min(WIDTH - 1, max(x1, x2))
        val startY = max(0, min(y1, y2))
        val endY = min(HEIGHT - 1, max(y1, y2))
        val c = color and NIBBLE_MASK

        for (y in startY..endY) {
            for (x in startX..endX) {
                setPixel(x, y, c)
            }
        }
    }

    fun print(text: String, x: Int, y: Int, color: Int) {
        fontRenderer.renderText(this, text, x, y, color)
    }

    fun updateFramebuffer() {
        val bufferData: ByteArray = framebuffer.toMapColors()
        var gridIndex = 0
        var bufferIndex = 0
        while (gridIndex < grid.size) {
            val pair = grid[gridIndex].toInt() and 0xFF  // AND with 0xFF to clear sign extension
            bufferData[bufferIndex++] = COLOR_CACHE[(pair shr 4) and 0xF]  // ensure index is within bounds
            bufferData[bufferIndex++] = COLOR_CACHE[pair and NIBBLE_MASK]
            gridIndex++
        }
    }
    fun getFramebuffer(): DirectFramebuffer = framebuffer
}

fun sendFramebuffer(player: Player, pixelGrid: PixelGrid, gameId: Int) {
    pixelGrid.updateFramebuffer()
    player.sendPacket(pixelGrid.getFramebuffer().preparePacket(gameId))
}