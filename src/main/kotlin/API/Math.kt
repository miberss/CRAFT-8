package API

import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import kotlin.math.*

fun MathAPI(lua: Globals) {
    lua.set("cos", object : OneArgFunction() {
        override fun call(angle: LuaValue): LuaValue {
            val rad = angle.todouble() * 2 * Math.PI // Convert to radians
            return LuaValue.valueOf(cos(rad))
        }
    })

    lua.set("sin", object : OneArgFunction() {
        override fun call(angle: LuaValue): LuaValue {
            val rad = angle.todouble() * 2 * Math.PI // Convert to radians
            return LuaValue.valueOf(-sin(rad)) // Invert to match PICO-8
        }
    })

    lua.set("flr", object : OneArgFunction() {
        override fun call(number: LuaValue): LuaValue {
            return LuaValue.valueOf(floor(number.todouble()))
        }
    })

    lua.set("sqrt", object : OneArgFunction() {
        override fun call(value: LuaValue): LuaValue {
            return LuaValue.valueOf(sqrt(value.todouble()))
        }
    })

    lua.set("abs", object : OneArgFunction() {
        override fun call(value: LuaValue): LuaValue {
            return LuaValue.valueOf(abs(value.todouble()))
        }
    })

    lua.set("sgn", object : OneArgFunction() {
        override fun call(value: LuaValue): LuaValue {
            return LuaValue.valueOf(sign(value.todouble()))
        }
    })

    lua.set("ceil", object : OneArgFunction() {
        override fun call(value: LuaValue): LuaValue {
            return LuaValue.valueOf(ceil(value.todouble()))
        }
    })

    lua.set("max", object : LuaFunction() {
        override fun invoke(args: Varargs): LuaValue {
            if (args.narg() < 1) {
                throw LuaError("max expects at least 1 argument (first, [second])")
            }
            val first = args.arg(1).todouble()
            val second = if (args.narg() >= 2) args.arg(2).todouble() else 0.0
            return LuaValue.valueOf(Math.max(first, second))
        }
    })

    lua.set("min", object : LuaFunction() {
        override fun invoke(args: Varargs): LuaValue {
            if (args.narg() < 1) {
                throw LuaError("min expects at least 1 argument (first, [second])")
            }
            val first = args.arg(1).todouble()
            val second = if (args.narg() >= 2) args.arg(2).todouble() else 0.0
            return LuaValue.valueOf(Math.min(first, second))
        }
    })

    lua.set("mid", object : LuaFunction() {
        override fun invoke(args: Varargs): LuaValue {
            val first = args.arg(1).todouble()
            val second = args.arg(2).todouble()
            val third = args.arg(3).todouble()
            return LuaValue.valueOf(listOf(first, second, third).sorted()[1])
        }
    })
}
