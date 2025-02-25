package API

import me.mibers.PixelGrid
import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua

fun GraphicsAPI(lua: Lua, pixelGrid: PixelGrid) {
    lua.set("pset", JFunction { state: Lua ->
        val x = state.toNumber(1).toInt()
        val y = state.toNumber(2).toInt()
        if (!state.isNil(3)) {
            pixelGrid.color = state.toNumber(3).toInt()
        }
        pixelGrid.setPixel(x, y)
        0
    })

    lua.set("pget", JFunction { state: Lua ->
        val x = state.toNumber(1).toInt()
        val y = state.toNumber(2).toInt()
        state.push(pixelGrid.getPixel(x, y))
        1
    })

    lua.set("cls", JFunction { state: Lua ->
        if (!state.isNil(1)) {
            pixelGrid.color = state.toNumber(1).toInt()
        }
        pixelGrid.clear()
        0
    })

    lua.set("circfill", JFunction { state: Lua ->
        val x = state.toNumber(1).toInt()
        val y = state.toNumber(2).toInt()
        val radius = state.toNumber(3).toInt()
        if (!state.isNil(4)) {
            pixelGrid.color = state.toNumber(4).toInt()
        }
        pixelGrid.circ(x, y, radius, true)
        0
    })

    lua.set("circ", JFunction { state: Lua ->
        val x = state.toNumber(1).toInt()
        val y = state.toNumber(2).toInt()
        val radius = state.toNumber(3).toInt()
        if (!state.isNil(4)) {
            pixelGrid.color = state.toNumber(4).toInt()
        }
        pixelGrid.circ(x, y, radius)
        0
    })

    lua.set("rect", JFunction { state: Lua ->
        val x1 = state.toNumber(1).toInt()
        val y1 = state.toNumber(2).toInt()
        val x2 = state.toNumber(3).toInt()
        val y2 = state.toNumber(4).toInt()
        if (!state.isNil(5)) {
            pixelGrid.color = state.toNumber(5).toInt()
        }
        pixelGrid.rect(x1, y1, x2, y2)
        0
    })

    lua.set("rectfill", JFunction { state: Lua ->
        val x1 = state.toNumber(1).toInt()
        val y1 = state.toNumber(2).toInt()
        val x2 = state.toNumber(3).toInt()
        val y2 = state.toNumber(4).toInt()
        if (!state.isNil(5)) {
            pixelGrid.color = state.toNumber(5).toInt()
        }
        pixelGrid.rectFill(x1, y1, x2, y2)
        0
    })

    lua.set("line", JFunction { state: Lua ->
        val x1 = state.toNumber(1).toInt()
        val y1 = state.toNumber(2).toInt()
        val x2 = state.toNumber(3).toInt()
        val y2 = state.toNumber(4).toInt()
        if (!state.isNil(5)) {
            pixelGrid.color = state.toNumber(5).toInt()
        }
        pixelGrid.line(x1, y1, x2, y2)
        0
    })

    lua.set("print", JFunction { state: Lua ->
        val text = state.toString(1).toString()
        val x = state.toNumber(2).toInt()
        val y = state.toNumber(3).toInt()
        if (!state.isNil(4)) {
            pixelGrid.color = state.toNumber(4).toInt()
        }
        pixelGrid.print(text, x, y)
        0
    })

    lua.set("col", JFunction { state: Lua ->
        val previousColor = pixelGrid.color
        pixelGrid.color = state.toNumber(1).toInt()
        state.push(previousColor)
        1
    })
}