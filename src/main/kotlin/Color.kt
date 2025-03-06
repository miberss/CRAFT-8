package me.mibers

import net.kyori.adventure.text.format.TextColor

object Color {
    private val COLORS = byteArrayOf(
        119, 103, -43, -24,
        -106, 27, 14, 58,
        18, 62, 74, -122,
        -126, 35, 82, 10
    )
    fun getByte(index: Int) = COLORS[index and 0xF]
}

object RGB {
    val BLACK = TextColor.color(0, 0, 0)
    val DARK_BLUE = TextColor.color(29, 43, 83)
    val DARK_PURPLE = TextColor.color(126, 37, 83)
    val DARK_GREEN = TextColor.color(0, 135, 81)
    val BROWN = TextColor.color(171, 82, 54)
    val DARK_GRAY = TextColor.color(95, 87, 79)
    val LIGHT_GRAY = TextColor.color(194, 195, 199)
    val WHITE = TextColor.color(255, 241, 232)
    val RED = TextColor.color(255, 0, 77)
    val ORANGE = TextColor.color(255, 163, 0)
    val YELLOW = TextColor.color(255, 236, 39)
    val GREEN = TextColor.color(0, 228, 54)
    val BLUE = TextColor.color(41, 173, 255)
    val LAVENDER = TextColor.color(131, 118, 156)
    val PINK = TextColor.color(255, 119, 168)
    val LIGHT_PEACH = TextColor.color(255, 204, 170)
}