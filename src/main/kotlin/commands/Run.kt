package commands

import me.mibers.RGB
import me.mibers.loadedGames
import me.mibers.runGame
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

class Run : Command("run") {
    init {
        addSyntax({ sender, _ ->
            if (sender !is Player) {
                sender.sendMessage(Component.text("Only players can run games!").color(RGB.RED))
                return@addSyntax
            }

            val code = loadedGames[sender]

            if (code == null) {
                sender.sendMessage(Component.text("No script loaded! Use /load <game> first.").color(RGB.RED))
                return@addSyntax
            }
            runGame(sender)
        })
    }
}
