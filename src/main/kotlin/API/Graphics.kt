package API

import me.mibers.PixelGrid
import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction

fun GraphicsAPI(lua: Globals, pixelGrid: PixelGrid) {
    lua.set("pset", object : ThreeArgFunction() {
        override fun call(x: LuaValue, y: LuaValue, color: LuaValue): LuaValue {
            pixelGrid.setPixel(x.toint(), y.toint(), color.toint())
            return LuaValue.NIL
        }
    })
    lua.set("pget", object : TwoArgFunction() {
        override fun call(x: LuaValue, y: LuaValue): LuaValue {
            return LuaValue.valueOf(pixelGrid.getPixel(x.toint(), y.toint()))
        }
    })
    lua.set("cls", object : OneArgFunction() {
        override fun call(color: LuaValue): LuaValue {
            pixelGrid.clear(color.toint())
            return LuaValue.NIL
        }
    })
    lua.set("circfill", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() != 4) {
                throw LuaError("circfill expects 4 arguments (x, y, radius, color)")
            }
            val x = args.arg(1).checkint()
            val y = args.arg(2).checkint()
            val radius = args.arg(3).checkint()
            val color = args.arg(4).checkint()
            pixelGrid.circFill(x, y, radius, color)
            return LuaValue.NIL
        }
    })
    lua.set("circ", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() != 4) {
                throw LuaError("circ expects 4 arguments (x, y, radius, color)")
            }
            val x = args.arg(1).checkint()
            val y = args.arg(2).checkint()
            val radius = args.arg(3).checkint()
            val color = args.arg(4).checkint()
            pixelGrid.circ(x, y, radius, color)
            return LuaValue.NIL
        }
    })
    lua.set("rect", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() != 5) {
                throw LuaError("rect expects 5 arguments (x1, y1, x2, y2, color)")
            }
            val x1 = args.arg(1).checkint()
            val y1 = args.arg(2).checkint()
            val x2 = args.arg(3).checkint()
            val y2 = args.arg(4).checkint()
            val color = args.arg(5).checkint()
            pixelGrid.rect(x1, y1, x2, y2, color)
            return LuaValue.NIL
        }
    })
    lua.set("rectfill", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() != 5) {
                throw LuaError("rectfill expects 5 arguments (x1, y1, x2, y2, color)")
            }
            val x1 = args.arg(1).checkint()
            val y1 = args.arg(2).checkint()
            val x2 = args.arg(3).checkint()
            val y2 = args.arg(4).checkint()
            val color = args.arg(5).checkint()
            pixelGrid.rectFill(x1, y1, x2, y2, color)
            return LuaValue.NIL
        }
    })
    lua.set("line", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() != 5) {
                throw LuaError("line expects 5 arguments (x1, y1, x2, y2, color)")
            }
            val x1 = args.arg(1).checkint()
            val y1 = args.arg(2).checkint()
            val x2 = args.arg(3).checkint()
            val y2 = args.arg(4).checkint()
            val color = args.arg(5).checkint()
            pixelGrid.line(x1, y1, x2, y2, color)
            return LuaValue.NIL
        }
    })
    lua.set("print", object : LuaFunction() {
        override fun invoke(args: Varargs): Varargs {
            if (args.narg() < 3) {
                throw LuaError("print expects at least 3 arguments (text, x, y, [color])")
            }
            val text = args.arg(1).checkjstring()
            val x = args.arg(2).checkint()
            val y = args.arg(3).checkint()
            val color = if (args.narg() >= 4) args.arg(4).checkint() else 8
            pixelGrid.print(text, x, y, color)
            return LuaValue.NIL
        }
    })
}