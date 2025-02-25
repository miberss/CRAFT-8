package API

import party.iroiro.luajava.JFunction
import party.iroiro.luajava.Lua

fun InputAPI(lua: Lua, inputMapping: Map<Int, String>, playerInputs: Map<String, Boolean>) {
    lua.set("btn", JFunction { state: Lua ->
        val buttonNumber = state.toInteger(1).toInt()
        val inputName = inputMapping[buttonNumber]
        val isPressed = inputName?.let { playerInputs[it] } ?: false
        state.push(isPressed)
        1
    })
}