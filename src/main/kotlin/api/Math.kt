package api

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua
import kotlin.math.*

fun mathAPI(lua: Lua) {
    lua.set("cos", JFunction { state: Lua ->
        val angle = state.toNumber(1)
        val rad = angle * 2 * PI // Convert to radians
        state.push(cos(rad))
        1
    })

    lua.set("sin", JFunction { state: Lua ->
        val angle = state.toNumber(1)
        val rad = angle * 2 * PI // Convert to radians
        state.push(-sin(rad)) // Invert to match PICO-8
        1
    })

    lua.set("atan2", JFunction { state: Lua ->
        val y = state.toNumber(1)
        val x = state.toNumber(2)
        state.push(atan2(x, y))
        1
    })

    lua.set("flr", JFunction { state: Lua ->
        val number = state.toNumber(1)
        state.push(floor(number))
        1
    })

    lua.set("rnd", JFunction { state: Lua ->
        val max = state.toNumber(1)
        state.push(Math.random() * max)
        1
    })

    lua.set("sqrt", JFunction { state: Lua ->
        val value = state.toNumber(1)
        state.push(sqrt(value))
        1
    })

    lua.set("abs", JFunction { state: Lua ->
        val value = state.toNumber(1)
        state.push(abs(value))
        1
    })

    lua.set("sgn", JFunction { state: Lua ->
        val value = state.toNumber(1)
        state.push(sign(value))
        1
    })

    lua.set("ceil", JFunction { state: Lua ->
        val value = state.toNumber(1)
        state.push(ceil(value))
        1
    })

    lua.set("max", JFunction { state: Lua ->
        val first = state.toNumber(1)
        val second = if (state.isNil(2)) 0.0 else state.toNumber(2)
        state.push(max(first, second))
        1
    })

    lua.set("min", JFunction { state: Lua ->
        val first = state.toNumber(1)
        val second = if (state.isNil(2)) 0.0 else state.toNumber(2)
        state.push(min(first, second))
        1
    })

    lua.set("mid", JFunction { state: Lua ->
        val first = state.toNumber(1)
        val second = state.toNumber(2)
        val third = state.toNumber(3)
        state.push(listOf(first, second, third).sorted()[1])
        1
    })
}