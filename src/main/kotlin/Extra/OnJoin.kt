package me.mibers.Extra

import me.mibers.mapCounter
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.*
import net.minestom.server.event.GlobalEventHandler
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSkinInitEvent
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.item.ItemComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.timer.TaskSchedule

fun onJoin(eventHandler: GlobalEventHandler, instanceContainer: InstanceContainer) {
    eventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 41.0, 0.0)
        player.gameMode = GameMode.CREATIVE
        giveMap(player)
        MinecraftServer.getSchedulerManager().scheduleTask({
            createSeat(player, instanceContainer)
        }, TaskSchedule.tick(20), TaskSchedule.stop()) // Run after 20 ticks, then stop
    }

    eventHandler.addListener(PlayerSkinInitEvent::class.java) { event ->
        event.skin = PlayerSkin.fromUsername(event.player.username)
    }
}
fun createSeat(player: Player, instance: InstanceContainer) {
    val seat = Entity(EntityType.ITEM_DISPLAY)
    seat.setInstance(instance, player.position)
    seat.addPassenger(player)
}
fun giveMap(player: Player) {
    val map = ItemStack.builder((Material.FILLED_MAP))
        .set(ItemComponent.MAP_ID, mapCounter)
        .build()
    player.itemInMainHand = map
}
