package commands

import net.minestom.server.MinecraftServer

fun initCommands() {
    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(Run())
    commandManager.register(Load())
    commandManager.register(Link())
}