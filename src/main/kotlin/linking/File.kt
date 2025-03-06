package linking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FileData(
    val name: String,
    val extension: String,
    val content: String,
    @Json(name = "discord_id") val discordId: String
)