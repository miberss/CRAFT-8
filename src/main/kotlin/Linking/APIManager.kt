package Linking

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import java.util.concurrent.TimeUnit

object APIManager {

    private val properties = Properties().apply {
        val inputStream = APIManager::class.java.classLoader.getResourceAsStream("config.properties")
        if (inputStream != null) {
            load(inputStream)
        } else {
            throw IllegalStateException("Could not find config.properties in resources")
        }
    }

    private val url = properties.getProperty("URL")
    private val admin_key = properties.getProperty("ADMIN_KEY")

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    fun createScript(name: String, extension: String, content: String, discordId: String): String {
        val json = "application/json; charset=utf-8".toMediaType()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(FileData::class.java)
        val fileData = FileData(name, extension, content, discordId)
        val jsonBody = jsonAdapter.toJson(fileData)
        val requestBody = jsonBody.toRequestBody(json)

        val request = Request.Builder()
            .url("${this.url}/admin/scripts")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: return "Error: Empty Response"
                if (response.isSuccessful) {
                    return responseBody
                } else {
                    return "Error: ${response.code} - $responseBody"
                }
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun getUserByCode(code: Int): UserResponse {
        val request = Request.Builder()
            .url("${this.url}/users/findCode?code=$code")
            .addHeader("Cookie", "admin_key=${this.admin_key}")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            try {
                val body = response.body?.string() ?: return UserResponse.Failure("Empty response")
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val successAdapter = moshi.adapter(UserResponse.Success::class.java)
                val successResponse = successAdapter.fromJson(body)
                if (successResponse != null) {
                    return successResponse
                }
                return UserResponse.Failure("Invalid response format")
            } catch (error: Exception) {
                println(error)
                return UserResponse.Failure("Code not found")
            }
        }
    }

    fun verifyUser(code: Int, uuid: String, username: String): VerificationResponse {
        val json = "application/json; charset=utf-8".toMediaType()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(VerificationRequest::class.java)
        val jsonBody = jsonAdapter.toJson(VerificationRequest(code, uuid, username))

        val requestBody = jsonBody.toRequestBody(json)

        val request = Request.Builder()
            .url("${this.url}/users/verify")
            .addHeader("Cookie", "admin_key=${this.admin_key}")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: return VerificationResponse.Failure("Empty response")

            val moshiBuilder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val successAdapter = moshiBuilder.adapter(VerificationResponse.Success::class.java)

            val successResponse = successAdapter.fromJson(body)

            if (successResponse != null) {
                return successResponse
            }

            return VerificationResponse.Failure("Invalid response format")
        }
    }
    fun getScriptByName(mcUUID: String, scriptName: String): String? {
        val url = "${this.url}/users/$mcUUID/scripts?useUUID=true"

        val request = Request.Builder()
            .url(url)
            .addHeader("Cookie", "admin_key=${this.admin_key}")
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: return null
                if (!response.isSuccessful) {
                    return null
                }
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val adapter = moshi.adapter<List<FileData>>(Types.newParameterizedType(List::class.java, FileData::class.java))
                val scripts = adapter.fromJson(responseBody) ?: return null
                val script = scripts.find { it.name == scriptName }
                return script?.content  // return content of the found script or null if not found
            }
        } catch (e: Exception) {
            null
        }
    }
}