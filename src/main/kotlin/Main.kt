package me.mibers

import commands.initCommands
import me.mibers.Extra.initTpsMonitor
import me.mibers.Extra.initInstance
import me.mibers.Extra.onJoin
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth

fun main() {
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()
    val eventHandler = MinecraftServer.getGlobalEventHandler()
    initInstance(instanceContainer)
    initCommands()
    initTpsMonitor()
    onJoin(eventHandler, instanceContainer)
    MojangAuth.init()
    minecraftServer.start("0.0.0.0", 25565)
}