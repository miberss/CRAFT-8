package me.mibers.Commands

import Commands.Link
import net.minestom.server.MinecraftServer

fun initCommands() {
    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(RunCommand())
    commandManager.register(LoadCommand())
    commandManager.register(Link())
}