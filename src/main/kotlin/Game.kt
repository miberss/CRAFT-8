package me.mibers

import API.*
import API.TableAPI
import kotlinx.coroutines.*
import me.mibers.Extra.giveMap
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import party.iroiro.luajava.LuaException
import party.iroiro.luajava.luajit.LuaJit
import java.util.concurrent.ConcurrentHashMap
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
    var task: Task? = null

    // using ConcurrentHashMap for thread-safe player inputs
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
    private val luaLock = ReentrantLock()
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
        task?.cancel()
        task = null
        updateFn = null
        drawFn = null
    }

    fun getPixelGrid(): PixelGrid {
        return pixelGrid
    }
}

fun loadGame(player: Player, script: String) {
    mapCounter += 1

    val game = activeGames[player]
    game?.shutdown()
    game?.task?.cancel()
    game?.task = null
    activeGames.remove(player)

    // Store the newly loaded script
    loadedGames[player] = script
}

fun runGame(player: Player) {
    val code = loadedGames[player]

    if (code == null) {
        player.sendMessage("No game loaded! Use /load <game> first.")
        return
    }

    val currentGame = activeGames[player]
    currentGame?.shutdown()
    activeGames.remove(player)
    giveMap(player)

    val game = Game(mapCounter)
    game.loadScript(code)
    activeGames[player] = game

    var lastTime = System.nanoTime()

    game.task = MinecraftServer.getSchedulerManager().scheduleTask({
        if (!activeGames.containsKey(player)) {
            game.shutdown()
            return@scheduleTask
        }

        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastTime) / 1_000_000_000.0
        lastTime = currentTime

        println("Game ID: ${game.id}, deltaTime: $deltaTime")

        game.update(deltaTime)

        game.getPixelGrid().swapBuffers()
        sendFramebuffer(player, game.getPixelGrid(), game.id)

        if (game.updateFn == null && game.drawFn == null) {
            game.shutdown()
            return@scheduleTask
        }

        val inputs = mapOf(
            "jump" to player.inputs().jump(),
            "sprint" to player.inputs().sprint(),
            "right" to player.inputs().right(),
            "backward" to player.inputs().backward(),
            "left" to player.inputs().left(),
            "forward" to player.inputs().forward()
        )
        game.updatePlayerInputs(inputs)

        game.draw() // keep at back
    }, TaskSchedule.tick(1), TaskSchedule.tick(1))
}