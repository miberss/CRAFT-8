package me.mibers.Extra

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.event.server.ServerTickMonitorEvent
import net.minestom.server.monitoring.TickMonitor
import net.minestom.server.utils.MathUtils.round
import net.minestom.server.utils.time.TimeUnit
import java.util.concurrent.atomic.AtomicReference

fun initTpsMonitor() {
    val lastTick = AtomicReference<TickMonitor>()
    MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent::class.java) { event ->
        lastTick.set(event.tickMonitor)
    }
    MinecraftServer.getSchedulerManager().buildTask {
        val tickMonitor = lastTick.get()
        if (tickMonitor == null || MinecraftServer.getConnectionManager().onlinePlayers.isEmpty()) {
            return@buildTask
        }
        val benchmarkManager = MinecraftServer.getBenchmarkManager()
        val ramUsage = benchmarkManager.usedMemory / 1e6

        val message = Component.text("${round(ramUsage, 3)}MB ")
            .append(Component.text("${round(tickMonitor.tickTime, 3)}ms "))
        Audiences.players().sendActionBar(message.color(TextColor.color(0xEF7DA3)))
    }.repeat(1, TimeUnit.SERVER_TICK).schedule()
}
