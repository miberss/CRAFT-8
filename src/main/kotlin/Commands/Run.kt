package me.mibers.Commands

import me.mibers.Color
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

/*
 * PICO8
 * Run
 * Created by mibers on 2/17/2025.
*/

class RunCommand() : Command("run") {
    init {
        addSyntax({ sender, _ ->
            val player = sender as Player
            player.sendMessage(Component.text("Ran `run`").color(Color.RGB.RED))
        })
    }
}