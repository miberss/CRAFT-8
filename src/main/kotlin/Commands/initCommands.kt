package me.mibers.Commands

import net.minestom.server.MinecraftServer

/*
 * PICO8
 * initCommands
 * Created by mibers on 2/17/2025.
*/

fun initCommands() {
    val commandManager = MinecraftServer.getCommandManager()
    commandManager.register(RunCommand())
    commandManager.register(LoadCommand())
}