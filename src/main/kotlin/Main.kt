package me.mibers

import me.mibers.Commands.initCommands
import me.mibers.Extra.initTpsMonitor
import me.mibers.Extra.initInstance
import me.mibers.Extra.onJoin
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule

fun main() {
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    initInstance(instanceContainer)
    initCommands()
    initTpsMonitor()
    initGame()
    onJoin(eventHandler, instanceContainer)
    minecraftServer.start("0.0.0.0", 25565)
}


fun initGame() {
    val game = Game()
    game.loadScript("""
local x_center = 0
local y_center = 0
local zoom = 1
local max_iter = 30
local speed = 0.05
local zoom_speed = 1.1
local width = 127
local height = 127
local x_scale = 3 / width
local y_scale = 3 / height
local c_real = -0.7
local c_imag = 0.27015
local bailout = 4

pixel_data = {}
for y = 0, height do
    pixel_data[y] = {}
end

function _update()
    local move = speed / zoom
    if btn(0) then x_center = x_center - move end
    if btn(1) then x_center = x_center + move end
    if btn(2) then y_center = y_center - move end
    if btn(3) then y_center = y_center + move end
    if btn(4) then zoom = zoom * zoom_speed
    elseif btn(5) then zoom = zoom / zoom_speed end
    
    local x_scale_zoom = x_scale / zoom
    local y_scale_zoom = y_scale / zoom
    local x_offset = x_center - 2 / zoom
    local y_offset = y_center - 1.5 / zoom
    for py = 0, height do
        local y0 = py * y_scale_zoom + y_offset
        for px = 0, width do
            local x0 = px * x_scale_zoom + x_offset
            local x, y = x0, y0
            local x2, y2 = x * x, y * y
            local iter = 0
            while iter < max_iter and (x2 + y2 <= bailout) do
                y = 2 * x * y + c_imag
                x = x2 - y2 + c_real
                x2 = x * x
                y2 = y * y
                iter = iter + 1
            end
            pixel_data[py][px] = iter == max_iter and 0 or (iter % 15) + 1
        end
    end
end

function _draw()
    for py = 0, height do
        for px = 0, width do
            pset(px, py, pixel_data[py][px])
        end
    end
end
""")
    MinecraftServer.getSchedulerManager().scheduleTask({
        game.update(0.05)
        game.draw()

        MinecraftServer.getConnectionManager().onlinePlayers.forEach { player ->
            sendFramebuffer(player, game.getPixelGrid())
            val inputs = mapOf(
                "jump" to player.inputs().jump(),
                "sprint" to player.inputs().sprint(),
                "right" to player.inputs().right(),
                "backward" to player.inputs().backward(),
                "left" to player.inputs().left(),
                "forward" to player.inputs().forward(),
            )
            game.updatePlayerInputs(inputs)
        }
    }, TaskSchedule.tick(1), TaskSchedule.tick(1)) // Run every tick
}