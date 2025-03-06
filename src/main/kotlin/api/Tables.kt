package api

import party.iroiro.luajava.Lua
import party.iroiro.luajava.JFunction

fun tableAPI(lua: Lua) {
    lua.set("add", JFunction { state ->
        if (!state.isTable(1)) {
            state.error("First argument must be a table")
        }
        if (state.top < 2) {
            state.error("add expects 2 arguments (table, value)")
        }
        val length = state.rawLength(1) // get the length of the table
        var index = 1
        while (index <= length) {
            state.rawGetI(1, index) // push table[index] onto the stack
            if (state.isNil(-1)) { // check if table[index] is nil
                state.pop(1) // remove the nil value from the stack
                break
            }
            state.pop(1) // remove the non-nil value from the stack
            index++
        }
        state.pushValue(2) // push the value at stack index 2 (the second argument)
        state.rawSetI(1, index) // set the value at the found index
        0
    })
}