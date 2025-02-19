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
function print_centered(str, y, col)
  print(str, 64 - (#str * 2), y, col) 
end

function _init()
  t = 0
  particles = {}
  message = "you win!"
  message_y = 120
  poly_rot = 0
  poly_scale = 1
  poly_col = 8
end

function _update()
  t = t + 0.1
  message_y = message_y - 0.5
  if message_y < 60 then message_y = 60 end
  
  -- Update particles
  for p in all(particles) do
    p.x = p.x + p.dx
    p.y = p.y + p.dy
    p.life = p.life - 1
    if p.life <= 0 then del(particles, p) end
  end
  
  -- Add new particles
  if t % 0.1 < 0.05 then
    add(particles, {
      x = 64 + math.sin(t) * 40,
      y = 64 + math.cos(t) * 40,
      dx = math.sin(t) * 2,
      dy = math.cos(t) * 2,
      life = 30,
      col = (t * 10) % 8 + 8
    })
  end
  
  -- Rotate and scale polygon
  poly_rot = poly_rot + 0.02
  poly_scale = 1 + math.sin(t) * 0.5
  poly_col = (t * 10) % 8 + 8
end

function _draw()
  cls()
  
  -- Layer 1: Rainbow sine wave
  for x = 0, 128 do
    y = 64 + math.sin(x / 16 + t) * 20
    col = (x + t * 10) % 8 + 8
    pset(x, y, col)
  end
  
  -- Layer 2: Rotating and scaling polygon
  draw_polygon(64, 64, 30, 6, poly_rot, poly_scale, poly_col)
  
  -- Layer 3: Particles
  for p in all(particles) do
    circfill(p.x, p.y, 2, p.col)
  end
  
  -- Layer 4: Scrolling text
  scroll_x = (t * 20) % 128
  print("this is a super cool demo! ", 128 - scroll_x, 10, 7)
  print("this is a super cool demo! ", 256 - scroll_x, 10, 7)
  
  -- Layer 5: Bouncing message
  print_centered(message, message_y, 12)
  
  
  -- Layer 7: Interactive reset text
  print_centered("press x to reset", 100, 8)
  if btn(5) then
    message_y = 120
  end
end

function draw_polygon(x, y, radius, sides, rot, scale, col)
  local angle = 2 * math.pi / sides
  local pts = {}
  for i = 0, sides do
    local px = x + math.cos(i * angle + rot) * radius * scale
    local py = y + math.sin(i * angle + rot) * radius * scale
    add(pts, px)
    add(pts, py)
  end
  for i = 1, #pts - 2, 2 do
    line(pts[i], pts[i + 1], pts[i + 2], pts[i + 3], col)
  end
  line(pts[#pts - 1], pts[#pts], pts[1], pts[2], col)
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