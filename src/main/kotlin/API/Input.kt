package API

import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

/*
 * PICO8
 *
 * Created by mibers on 2/18/2025.
 */

fun InputAPI(lua: Globals, inputMapping: Map<Int, String>, playerInputs: Map<String, Boolean>) {
    lua.set("btn", object : OneArgFunction() {
        override fun call(button: LuaValue): LuaValue {
            val buttonNumber = button.toint()
            val inputName = inputMapping[buttonNumber] ?: return LuaValue.FALSE
            return LuaValue.valueOf(playerInputs[inputName] ?: false)
        }
    })
}