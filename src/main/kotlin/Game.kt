package me.mibers

import API.GraphicsAPI
import API.InputAPI
import API.MathAPI
import API.TimeAPI
import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.JsePlatform
import java.util.concurrent.Executors
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

    private val tickExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "GameTickThread").apply { isDaemon = true }
    }
    private val renderExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "GameRenderThread").apply { isDaemon = true }
    }

    private val stateLock = ReentrantLock()

    private val playerInputs = mutableMapOf<String, Boolean>().withDefault { false }

    init {
        setupLuaAPI()
    }
    private fun setupLuaAPI() {
        GraphicsAPI(lua, pixelGrid)
        MathAPI(lua)
        TimeAPI(lua, time)
        InputAPI(lua, inputMapping, playerInputs)
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
        tickExecutor.submit {
            stateLock.withLock {
                time += deltaTime
                try {
                    updateFn?.call(LuaValue.valueOf(deltaTime))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun draw() {
        renderExecutor.submit {
            stateLock.withLock {
                try {
                    drawFn?.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    fun updatePlayerInputs(inputs: Map<String, Boolean>) {
        tickExecutor.submit {
            stateLock.withLock {
                playerInputs.clear()
                playerInputs.putAll(inputs)
            }
        }
    }
    fun shutdown() {
        tickExecutor.shutdown()
        renderExecutor.shutdown()
    }
    fun getPixelGrid(): PixelGrid {
        return pixelGrid
    }
}

