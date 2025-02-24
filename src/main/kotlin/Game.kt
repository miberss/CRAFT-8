package me.mibers

import API.*
import org.luaj.vm2.*
import org.luaj.vm2.lib.jse.JsePlatform
import kotlinx.coroutines.*
import me.mibers.Extra.giveMap
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.timer.Task
import net.minestom.server.timer.TaskSchedule
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

val activeGames = mutableMapOf<Player, Game>()
val loadedGames = mutableMapOf<Player, String>()
var mapCounter = 0

class Game(val id: Int) {
    private val pixelGrid = PixelGrid()
    private val lua: Globals = JsePlatform.standardGlobals()
    var updateFn: LuaValue? = null
    var drawFn: LuaValue? = null
    private var time: Double = 0.0
    var task: Task? = null

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
        playerInputs.keys.forEach { playerInputs[it] = false }
        task = null
        updateFn = null
        drawFn = null
    }

    fun getPixelGrid(): PixelGrid {
        return pixelGrid
    }
}


fun loadGame(player: Player, script: String) {
    // stop and remove any running game before starting a new one
    mapCounter += 1
    val game = activeGames[player]
    game?.shutdown()
    game?.task?.cancel()
    game?.task = null
    activeGames.remove(player)

    // store the newly loaded script
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

        println(game.id)

        game.update(deltaTime)
        game.draw()
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
    }, TaskSchedule.tick(1), TaskSchedule.tick(1))
}