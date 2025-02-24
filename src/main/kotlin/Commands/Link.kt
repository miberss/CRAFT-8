package Commands

import Linking.APIManager
import Linking.UserResponse
import Linking.VerificationResponse
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class Link : Command("link") {
    init {
        setDefaultExecutor { sender, _ ->
            if (sender !is Player) {
                sender.sendMessage("Only a player can execute this command.")
            } else {
                sender.sendMessage("Usage: /link <code>")
            }
        }

        val code = ArgumentType.Integer("code")

        addSyntax({ sender, context ->
            val codeCtx = context.get(code)
            val player = sender as Player

            val userResponse = APIManager.getUserByCode(codeCtx)

            println(userResponse)
            when (userResponse) {
                is UserResponse.Failure -> {
                    sender.sendMessage("Invalid code.")
                    return@addSyntax
                }

                is UserResponse.Success -> {
                    val verifyResponse = APIManager.verifyUser(codeCtx, sender.uuid.toString(), sender.username)

                    when (verifyResponse) {
                        is VerificationResponse.Failure -> {
                            sender.sendMessage("Failed to verify.")
                            return@addSyntax
                        }

                        is VerificationResponse.Success -> {
                            sender.sendMessage("Successfully verified.")
                            return@addSyntax
                        }
                    }
                }
            }
        }, code)
    }
}