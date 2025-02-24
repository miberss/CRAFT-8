package me.mibers.Commands

import Linking.APIManager
import me.mibers.Game
import me.mibers.RGB
import me.mibers.loadGame
import me.mibers.loadedGames
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class LoadCommand : Command("load") {
    init {
        val scriptArg = ArgumentType.String("gameName")

        addSyntax({ sender, args ->
            if (sender !is Player) {
                sender.sendMessage(Component.text("Only players can load games!").color(RGB.LIGHT_PEACH))
                return@addSyntax
            }

            val gameName = args.get(scriptArg)
            val script = APIManager.getScriptByName(sender.uuid.toString(), gameName)
            if (script != null) {
                loadedGames[sender] = gameName
                loadGame(sender, script)
                sender.sendMessage(Component.text("Loaded: $gameName").color(RGB.LIGHT_PEACH))
            } else {
                sender.sendMessage(Component.text("Something went wrong with finding the code in the database.").color(RGB.RED))
            }



        }, scriptArg)
    }
}
