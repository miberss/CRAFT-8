import java.io.File
import java.util.UUID

class GameStorage(private val storageDir: String) {

    init {
        // ensure the storage directory exists
        File(storageDir).mkdirs()
    }

    // get the game directory for player
    private fun getPlayerGameDir(playerId: UUID): File {
        val playerDir = File(storageDir, playerId.toString())
        playerDir.mkdirs() // Create the directory if it doesn't exist
        return playerDir
    }

    // upload a game file per player
    fun uploadGame(playerId: UUID, fileContent: ByteArray): String {
        val playerDir = getPlayerGameDir(playerId)
        val gameId = UUID.randomUUID().toString()
        val gameFile = File(playerDir, gameId)

        // write the file content to the player's directory
        gameFile.writeBytes(fileContent)

        // return the game ID for reference
        return gameId
    }

    // load a game file for a specific player
    fun loadGame(playerId: UUID, gameId: String): ByteArray? {
        val playerDir = getPlayerGameDir(playerId)
        val gameFile = File(playerDir, gameId)
        return if (gameFile.exists()) {
            gameFile.readBytes()
        } else {
            null // game not found
        }
    }

    // list all game IDs per player
    fun listGames(playerId: UUID): List<String> {
        val playerDir = getPlayerGameDir(playerId)
        return playerDir.listFiles()?.map { it.name } ?: emptyList()
    }

    // delete game for a specific player
    fun deleteGame(playerId: UUID, gameId: String): Boolean {
        val playerDir = getPlayerGameDir(playerId)
        val gameFile = File(playerDir, gameId)
        return if (gameFile.exists()) {
            gameFile.delete()
        } else {
            false // game not found
        }
    }
}