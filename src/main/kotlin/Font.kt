package me.mibers

class FontRenderer {
    private val font = loadFont()
    private val widths = IntArray(256) // To store the width of each character

    init {
        calculateWidths() // Calculate and store widths when the font is loaded
    }

    fun renderText(grid: PixelGrid, text: String, x: Int, y: Int, color: Int) {
        var cursorX = x
        var cursorY = y
        for (char in text) {
            if (char == '\n') {
                cursorX = x
                cursorY += 6 // Move to next line
                continue
            }
            val charIndex = char.code % 256
            val width = widths[charIndex] // Use pre-calculated width
            val charBitmap = font[charIndex]
            for (px in 0 until width) {
                for (py in 0 until 8) {
                    if (charBitmap[px + py * 8] != 0.toByte()) {
                        grid.setPixel(cursorX + px, cursorY + py, color)
                    }
                }
            }
            cursorX += width + 1 // Move cursor by the width of the character
        }
    }

    private fun loadFont(): Array<ByteArray> {
        val fontImage = javax.imageio.ImageIO.read(javaClass.getResource("/PICO-8-FONT.png"))
        val font = Array(256) { ByteArray(8 * 8) }
        for (i in 0 until 256) {
            val charX = (i % 16) * 8
            val charY = (i / 16) * 8
            for (py in 0 until 8) {
                for (px in 0 until 8) {
                    val color = fontImage.getRGB(charX + px, charY + py)
                    font[i][px + py * 8] = if (color and 0xFFFFFF != 0) 1 else 0
                }
            }
        }
        return font
    }

    // Calculate the width of each character based on the bitmap
    private fun calculateWidths() {
        for (i in 0 until 256) {
            val charBitmap = font[i]
            var width = 0
            // Check for the last non-zero column to determine the width
            for (px in 0 until 8) {
                for (py in 0 until 8) {
                    if (charBitmap[px + py * 8] != 0.toByte()) {
                        width = maxOf(width, px + 1) // Update width based on last non-zero pixel
                    }
                }
            }

            // If the character is blank (all pixels are 0), set its width to 4
            if (width < 3) {
                width = 3
            }

            widths[i] = width // Store the width of this character
        }
    }
}
