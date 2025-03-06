package linking

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
sealed class VerificationResponse {
    @JsonClass(generateAdapter = true)
    data class Success(
        val success: Boolean,
        val user: UserResponse.Success
    ) : VerificationResponse()

    @JsonClass(generateAdapter = true)
    data class Failure(
        val error: String
    ) : VerificationResponse()
}

@JsonClass(generateAdapter = true)
data class VerificationRequest(
    val code: Int,
    val uuid: String,
    val username: String
)