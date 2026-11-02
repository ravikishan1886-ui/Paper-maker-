package com.example.network

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GroqChatRequest(
    val model: String,
    val messages: List<GroqMessage>,
    val temperature: Double = 0.3,
    val max_tokens: Int = 4096
)

@JsonClass(generateAdapter = true)
data class GroqMessage(
    val role: String,
    val content: Any // String or List<Map<String, Any>> for multimodal
)

@JsonClass(generateAdapter = true)
data class GroqChatResponse(
    val id: String?,
    val choices: List<GroqChoice>?,
    val error: GroqError?
)

@JsonClass(generateAdapter = true)
data class GroqChoice(
    val index: Int?,
    val message: GroqChoiceMessage?
)

@JsonClass(generateAdapter = true)
data class GroqChoiceMessage(
    val role: String?,
    val content: String?
)

@JsonClass(generateAdapter = true)
data class GroqError(
    val message: String?,
    val type: String?,
    val code: String?
)

@JsonClass(generateAdapter = true)
data class GroqModelsResponse(
    val data: List<GroqModelItem>?
)

@JsonClass(generateAdapter = true)
data class GroqModelItem(
    val id: String
)

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: GroqChatRequest
    ): Response<GroqChatResponse>

    @GET("openai/v1/models")
    suspend fun listModels(
        @Header("Authorization") authHeader: String
    ): Response<GroqModelsResponse>

    companion object {
        private const val BASE_URL = "https://api.groq.com/"

        fun create(): GroqApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE // Protect keys from logging
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(GroqApiService::class.java)
        }
    }
}
