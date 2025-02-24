package Linking

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

sealed class UserResponse {
    @JsonClass(generateAdapter = true)
    data class Success(
        @Json(name = "discord_user") val discordUser: DiscordUser,
        @Json(name = "minecraft_user") val minecraftUser: MinecraftUser
    ) : UserResponse()

    @JsonClass(generateAdapter = true)
    data class Failure(
        val error: String
    ) : UserResponse()
}
@JsonClass(generateAdapter = true)
data class DiscordUser(
    val id: String,
    val username: String,
    val avatar: String,
    val discriminator: String,
    @Json(name = "global_name") val globalName: String,
    @Json(name = "avatar_decoration_data") val avatarDecorationData: DiscordUserDecoration?,
    val locale: String,
    @Json(name = "mfa_enabled") val mfaEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class DiscordUserDecoration(
    val asset: String,
    @Json(name = "sku_id") val skuId: String,
    @Json(name = "expires_at") val expiresAt: Long
)

@JsonClass(generateAdapter = true)
data class MinecraftUser(
    @Json(name = "current_username") val currentUsername: String,
    val uuid: String,
    @Json(name = "verified_at") val verifiedAt: Long,
    val verified: Boolean,
    val code: Int,
    @Json(name = "code_requested_at") val codeRequestedAt: Long
)