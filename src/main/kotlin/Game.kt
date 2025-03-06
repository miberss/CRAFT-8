package me.mibers

import api.*
import kotlinx.coroutines.*
import me.mibers.Extra.giveMap
import net.minestom.server.entity.Player
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luajit.LuaJit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

val activeGames = mutableMapOf<Player, Game>()
val loadedGames = mutableMapOf<Player, String>()
var mapCounter = 0

class Game(val id: Int) {
    private val pixelGrid = PixelGrid()
    private val lua: LuaJit = LuaJit().apply { openLibraries() }
    var updateFn: ((Double) -> Unit)? = null
    var drawFn: (() -> Unit)? = null
    private var time: Double = 0.0

    // using ConcurrentHashMap for thread-safe player inputs
    private val inputMapping = mapOf(
        0 to "left",
        1 to "right",
        2 to "forward",
        3 to "backward",
        4 to "sprint",
        5 to "jump"
    )

    private val playerInputs = ConcurrentHashMap<String, Boolean>().apply {
        inputMapping.values.forEach { put(it, false) }
    }

    private val timeLock = ReentrantLock()
    private val luaLock = ReentrantLock()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        setupLuaAPI()
    }

    private fun setupLuaAPI() {
        graphicsAPI(lua, pixelGrid)
        mathAPI(lua)
        timeAPI(lua, time)
        inputAPI(lua, inputMapping, playerInputs)
        tableAPI(lua)
    }

    fun loadScript(script: String) {
        try {
            luaLock.withLock {
                lua.load(script)
                lua.pCall(0, 0)
                lua.getGlobal("_init")
                if (lua.isFunction(-1)) {
                    lua.pCall(0, 0)
                } else {
                    lua.pop(1)
                }
                lua.getGlobal("_update")
                if (lua.isFunction(-1)) {
                    updateFn = { deltaTime: Double ->
                        luaLock.withLock {
                            lua.top = 0
                            lua.getGlobal("_update")
                            lua.push(deltaTime)
                            lua.pCall(1, 0)
                        }
                    }
                } else {
                    lua.pop(1)
                    updateFn = null
                }
                lua.getGlobal("_draw")
                if (lua.isFunction(-1)) {
                    drawFn = {
                        luaLock.withLock {
                            lua.top = 0
                            lua.getGlobal("_draw")
                            lua.pCall(0, 0)
                        }
                    }
                } else {
                    lua.pop(1)
                    drawFn = null
                }
            }
        } catch (e: LuaException) {
            e.printStackTrace()
        }
    }

    fun update(deltaTime: Double) {
        coroutineScope.launch {
            try {
                timeLock.withLock {
                    time += deltaTime
                }
                updateFn?.invoke(deltaTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun draw() {
        coroutineScope.launch {
            try {
                drawFn?.invoke()
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
        playerInputs.keys.forEach { playerInputs[it] = false }
        updateFn = null
        drawFn = null
    }

    fun getPixelGrid(): PixelGrid = pixelGrid
}

private val executor = Executors.newSingleThreadScheduledExecutor()

fun loadGame(player: Player, script: String) {
    mapCounter += 1
    activeGames[player]?.shutdown()
    activeGames.remove(player)
    loadedGames[player] = script
}

fun runGame(player: Player) {
    val code = loadedGames[player] ?: run {
        player.sendMessage("No game loaded! Use /load <game> first.")
        return
    }

    activeGames[player]?.shutdown()
    activeGames.remove(player)
    giveMap(player)

    val game = Game(mapCounter)
    game.loadScript(code)
    activeGames[player] = game

    var lastTime = System.nanoTime()

    executor.scheduleAtFixedRate({
        if (!activeGames.containsKey(player)) {
            game.shutdown()
            return@scheduleAtFixedRate
        }
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastTime) / 1_000_000_000.0
        lastTime = currentTime

        val inputs = mapOf(
            "jump" to player.inputs().jump(),
            "sprint" to player.inputs().sprint(),
            "right" to player.inputs().right(),
            "backward" to player.inputs().backward(),
            "left" to player.inputs().left(),
            "forward" to player.inputs().forward()
        )
        game.getPixelGrid().swapBuffers()
        sendFramebuffer(player, game.getPixelGrid(), game.id)
        game.updatePlayerInputs(inputs)
        game.update(deltaTime)
        game.draw()
    }, 0, 33, TimeUnit.MILLISECONDS)
}
