package me.mibers

import org.luaj.vm2.*
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.ThreeArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import kotlin.math.floor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Game {
    private val pixelGrid = PixelGrid()
    private val lua: Globals = JsePlatform.standardGlobals()
    private var updateFn: LuaValue? = null
    private var drawFn: LuaValue? = null
    var time: Double = 0.0

    private val inputMapping = mapOf(
        0 to "left",
        1 to "right",
        2 to "forward",
        3 to "backward",
        4 to "sprint",
        5 to "jump"
    )

    private val gameThreadPool = Executors.newFixedThreadPool(2, object : ThreadFactory {
        private val threadCounter = AtomicInteger(0)
        override fun newThread(r: Runnable): Thread {
            return Thread(r).apply {
                name = when (threadCounter.getAndIncrement()) {
                    0 -> "GameTickThread"
                    else -> "GameRenderThread"
                }
                isDaemon = true
            }
        }
    })

    private var renderThread: Thread? = null
    private var tickThread: Thread? = null
    private val stateLock = ReentrantLock()

    private val playerInputs = mutableMapOf<String, Boolean>().withDefault { false }

    init {
        setupLuaAPI()
        gameThreadPool.submit {
            tickThread = Thread.currentThread()
        }
        gameThreadPool.submit {
            renderThread = Thread.currentThread()
        }
    }
    private fun setupLuaAPI() {
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
        lua.set("t", object : ZeroArgFunction() {
            override fun call(): LuaValue {
                val current = (System.currentTimeMillis().toDouble() / 1000)
                val since = current - time
                return LuaValue.valueOf(since)
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
                val color = if (args.narg() >= 4) args.arg(4).checkint() else 7 // Default color
                pixelGrid.print(text, x, y, color)
                return LuaValue.NIL
            }
        })
        lua.set("cos", object : OneArgFunction() {
            override fun call(angle: LuaValue): LuaValue {
                val rad = angle.todouble() * 2 * Math.PI  // Convert to radians
                return LuaValue.valueOf(Math.cos(rad))
            }
        })

        lua.set("sin", object : OneArgFunction() {
            override fun call(angle: LuaValue): LuaValue {
                val rad = angle.todouble() * 2 * Math.PI  // Convert to radians
                return LuaValue.valueOf(-Math.sin(rad))  // Invert to match PICO-8
            }
        })
        lua.set("flr", object : OneArgFunction() {
            override fun call(number: LuaValue): LuaValue {
                return LuaValue.valueOf(floor(number.todouble()))
            }
        })
        lua.set("btn", object : OneArgFunction() {
            override fun call(button: LuaValue): LuaValue {
                val buttonNumber = button.toint()
                val inputName = inputMapping[buttonNumber] ?: return LuaValue.FALSE
                return LuaValue.valueOf(playerInputs[inputName] ?: false)
            }
        })
    }

    fun loadScript(script: String) {
        try {
            lua.load(script).call()
            updateFn = lua.get("_update")
            if (updateFn?.isfunction() != true) updateFn = null

            drawFn = lua.get("_draw")
            if (drawFn?.isfunction() != true) drawFn = null
        } catch (e: LuaError) {
            e.printStackTrace()
        }
    }

    fun update(deltaTime: Double) {
        time += 1 / 3
        // ensure we are on the tick thread
        if (Thread.currentThread() != tickThread) {
            gameThreadPool.submit {
                if (Thread.currentThread() == tickThread) {
                    performUpdate(deltaTime)
                }
            }
        } else {
            performUpdate(deltaTime)
        }
    }

    private fun performUpdate(deltaTime: Double) {
        try {
            stateLock.withLock {
                updateFn?.call(LuaValue.valueOf(deltaTime))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun draw() {
        // ensure we are on the render thread
        if (Thread.currentThread() != renderThread) {
            gameThreadPool.submit {
                if (Thread.currentThread() == renderThread) {
                    performDraw()
                }
            }
        } else {
            performDraw()
        }
    }

    private fun performDraw() {
        try {
            stateLock.withLock {
                drawFn?.call()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updatePlayerInputs(inputs: Map<String, Boolean>) {
        playerInputs.clear()
        playerInputs.putAll(inputs)
    }

    fun shutdown() {
        gameThreadPool.shutdown()
    }

    fun getPixelGrid(): PixelGrid = pixelGrid

    companion object {
        const val WIDTH = PixelGrid.WIDTH
        const val HEIGHT = PixelGrid.HEIGHT
    }
}

