package api

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua

fun timeAPI(lua: Lua, startTime: Double) {
    val getTimeFunction = JFunction { state: Lua ->
        val currentTime = System.currentTimeMillis().toDouble() / 1000
        val elapsedTime = currentTime - startTime
        state.push(elapsedTime)
        1
    }
    lua.set("t", getTimeFunction)
    lua.set("time", getTimeFunction)
}