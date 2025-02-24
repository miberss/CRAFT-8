package API

import org.luaj.vm2.*
import org.luaj.vm2.lib.VarArgFunction

fun TableAPI(lua: Globals) {
    lua.set("add", object : VarArgFunction() {
        override fun invoke(args: Varargs): LuaValue {
            if (args.narg() < 2) {
                throw LuaError("add expects 2 arguments (table, value)")
            }
            val tbl = args.checktable(1)
            val value = args.arg(2)
            tbl.set(tbl.length() + 1, value)
            return LuaValue.NIL
        }
    })

    lua.set("del", object : VarArgFunction() {
        override fun invoke(args: Varargs): LuaValue {
            if (args.narg() < 2) {
                throw LuaError("del expects 2 arguments (table, value)")
            }
            val tbl = args.checktable(1)
            val value = args.arg(2)
            val len = tbl.length()
            for (i in 1..len) {
                if (tbl[i].eq_b(value)) {
                    val deleted = tbl[i]
                    for (j in i until len) {
                        tbl.set(j, tbl[j + 1])
                    }
                    tbl.set(len, LuaValue.NIL)
                    return deleted
                }
            }
            return LuaValue.NIL
        }
    })

    lua.set("foreach", object : VarArgFunction() {
        override fun invoke(args: Varargs): LuaValue {
            if (args.narg() < 2) {
                throw LuaError("foreach expects 2 arguments (table, function)")
            }
            val tbl = args.checktable(1)
            val func = args.checkfunction(2)

            // Find the last non-nil index
            var maxIndex = 0
            var i = 1
            while (true) {
                val value = tbl.get(i)
                if (!value.isnil()) {
                    maxIndex = i
                } else if (i - maxIndex > 100) { break } // stop if we've gone 100 indices without finding a value
                i++
            }
            // iterate through all indices up to maxIndex
            for (i in 1..maxIndex) {
                val value = tbl.get(i)
                if (!value.isnil()) {
                    func.call(value)
                }
            }
            return LuaValue.NIL
        }
    })

    lua.set("all", object : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() < 1) throw LuaError("all expects 1 argument (table)")
            val tbl = args.checktable(1)
            return object : VarArgFunction() {
                var index = 0
                var lastSize = tbl.length()  // track table size to detect changes

                override fun invoke(args: Varargs): Varargs {
                    while (true) {
                        index++

                        // if the table size has changed
                        if (tbl.length() < lastSize) {
                            lastSize = tbl.length()  // update the last known size
                            index--  // adjust index to avoid skipping elements
                        }

                        val value = tbl.get(index)
                        if (value.isnil()) {
                            // stop if we've gone 100 indices without finding a value
                            if (index > 100 && tbl.get(index - 100).isnil()) {
                                return LuaValue.NIL
                            }
                            continue // skip nil
                        }

                        lastSize = tbl.length()  // update last size after accessing an element
                        return value // return the value (not nil)
                    }
                }
            }.let { iterator ->
                LuaValue.varargsOf(arrayOf(iterator, tbl, LuaValue.valueOf(0)))
            }
        }
    })
}