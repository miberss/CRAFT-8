package me.mibers

import API.*
import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.JsePlatform
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class Game {
    private val pixelGrid = PixelGrid()
    private val lua: Globals = JsePlatform.standardGlobals()
    private var updateFn: LuaValue? = null
    private var drawFn: LuaValue? = null
    private var time: Double = 0.0

    private val inputMapping = mapOf(
        0 to "left",
        1 to "right",
        2 to "forward",
        3 to "backward",
        4 to "sprint",
        5 to "jump"
    )

    // using ConcurrentHashMap for thread-safe player inputs
    private val playerInputs = ConcurrentHashMap<String, Boolean>().apply {
        inputMapping.values.forEach { put(it, false) }
    }

    private val timeLock = ReentrantLock()

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        setupLuaAPI()
    }

    private fun setupLuaAPI() {
        GraphicsAPI(lua, pixelGrid)
        MathAPI(lua)
        TimeAPI(lua, time)
        InputAPI(lua, inputMapping, playerInputs)
        TableAPI(lua)
    }

    fun loadScript(script: String) {
        try {
            lua.load(script).call()
            val initFn = lua.get("_init")
            if (initFn?.isfunction() == true) initFn.call()
            updateFn = lua.get("_update")
            if (updateFn?.isfunction() != true) updateFn = null

            drawFn = lua.get("_draw")
            if (drawFn?.isfunction() != true) drawFn = null
        } catch (e: LuaError) {
            e.printStackTrace()
        }
    }

    fun update(deltaTime: Double) {
        coroutineScope.launch {
            timeLock.withLock {
                time += deltaTime
            }
            try {
                updateFn?.call(LuaValue.valueOf(deltaTime))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun draw() {
        coroutineScope.launch {
            try {
                drawFn?.call()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePlayerInputs(inputs: Map<String, Boolean>) {
        coroutineScope.launch {
            inputs.forEach { (key, value) -> playerInputs[key] = value }
        }
    }

    fun shutdown() {
        coroutineScope.cancel()
    }

    fun getPixelGrid(): PixelGrid {
        return pixelGrid
    }
}
