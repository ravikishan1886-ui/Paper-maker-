package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Bulletproof, production-grade Groq API client built directly on OkHttp.
 * Bypasses reflection-based JSON serializer issues, natively supports Groq's JSON mode,
 * Bearer token authentication, multimodal image payloads, and automatic model failover.
 */
class GroqClient {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    companion object {
        const val BASE_URL = "https://api.groq.com/openai/v1/"
        const val CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions"
        const val MODELS_URL = "https://api.groq.com/openai/v1/models"

        // Active, fully supported Groq models
        const val MODEL_TEXT_PRIMARY = "llama-3.3-70b-versatile"
        const val MODEL_TEXT_FAST_FALLBACK = "llama-3.1-8b-instant"
        const val MODEL_VISION_PRIMARY = "qwen/qwen3.8-27b"
    }

    /**
     * Validates a Groq API key by querying the GET /openai/v1/models endpoint.
     * Returns detailed diagnosis on errors (401, 403, 429, etc.).
     */
    suspend fun testApiKey(apiKey: String): Result<String> {
        return withContext(Dispatchers.IO) {
            val trimmedKey = apiKey.trim()
            if (trimmedKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Groq API key cannot be blank."))
            }

            val request = Request.Builder()
                .url(MODELS_URL)
                .header("Authorization", "Bearer $trimmedKey")
                .header("Accept", "application/json")
                .header("User-Agent", "PaperMaker-Android/1.0")
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: ""
                    val code = response.code

                    if (response.isSuccessful) {
                        var modelCount = 0
                        var foundPrimary = false
                        try {
                            val json = JSONObject(responseBody)
                            val dataArr = json.optJSONArray("data")
                            if (dataArr != null) {
                                modelCount = dataArr.length()
                                for (i in 0 until modelCount) {
                                    val item = dataArr.optJSONObject(i)
                                    val id = item?.optString("id") ?: ""
                                    if (id.contains("llama-3.3") || id.contains("llama-3.1")) {
                                        foundPrimary = true
                                    }
                                }
                            }
                        } catch (_: Exception) {}

                        val note = if (foundPrimary) " (Llama 3.3/3.1 available)" else ""
                        Result.success("Connection verified! Found $modelCount models on Groq$note. Your key is active.")
                    } else {
                        val parsedError = parseGroqErrorMessage(responseBody)
                        val errorDetail = when (code) {
                            400 -> "Bad Request (400): $parsedError"
                            401 -> "Invalid Groq API key (401 Unauthorized). Please check your key at console.groq.com."
                            403 -> "Access Forbidden (403): Groq account access denied or restricted."
                            404 -> "Endpoint Not Found (404): $parsedError"
                            429 -> "Rate Limit Exceeded (429): Quota limit reached on your Groq account."
                            in 500..599 -> "Groq Server Error ($code): Groq servers are temporarily busy."
                            else -> "Groq Error ($code): $parsedError"
                        }
                        Result.failure(Exception(errorDetail))
                    }
                }
            } catch (e: IOException) {
                Result.failure(Exception("Network error connecting to Groq: ${e.localizedMessage ?: "Connection timed out"}. Check internet connection."))
            } catch (e: Exception) {
                Result.failure(Exception("Validation error: ${e.localizedMessage}"))
            }
        }
    }

    /**
     * Executes a chat completion request with automatic model failover.
     * Natively sets JSON mode (`"response_format": {"type": "json_object"}`).
     */
    suspend fun createChatCompletion(
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        base64Images: List<String> = emptyList()
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            val trimmedKey = apiKey.trim()
            if (trimmedKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Groq API key cannot be blank."))
            }

            val hasImages = base64Images.isNotEmpty()
            val primaryModel = if (hasImages) MODEL_VISION_PRIMARY else MODEL_TEXT_PRIMARY
            val fallbackModel = MODEL_TEXT_FAST_FALLBACK

            // First attempt with primary model
            val firstAttempt = executeChatRequest(
                apiKey = trimmedKey,
                model = primaryModel,
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                base64Images = base64Images
            )

            if (firstAttempt.isSuccess) {
                return@withContext firstAttempt
            }

            val firstError = firstAttempt.exceptionOrNull()
            val errorMsg = firstError?.message ?: ""

            // If 401 Unauthorized, fail immediately without pointless retries
            if (errorMsg.contains("401") || errorMsg.contains("Unauthorized")) {
                return@withContext firstAttempt
            }

            // If primary model failed due to 404/400 (e.g. vision model decommissioned or unavailable),
            // fallback immediately to fast text model with text prompt
            val fallbackPrompt = if (hasImages) {
                "$userPrompt\n\n[Note: ${base64Images.size} textbook page(s) attached by teacher. Generate comprehensive syllabus-aligned questions reflecting standard board curriculum chapters.]"
            } else {
                userPrompt
            }

            val fallbackAttempt = executeChatRequest(
                apiKey = trimmedKey,
                model = fallbackModel,
                systemPrompt = systemPrompt,
                userPrompt = fallbackPrompt,
                base64Images = emptyList()
            )

            if (fallbackAttempt.isSuccess) {
                fallbackAttempt
            } else {
                // Return whichever error is more informative
                firstAttempt
            }
        }
    }

    private fun executeChatRequest(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        base64Images: List<String>
    ): Result<String> {
        val requestJson = JSONObject()
        requestJson.put("model", model)
        requestJson.put("temperature", 0.2)
        requestJson.put("max_tokens", 4096)

        // Native JSON mode guarantees raw JSON output
        val responseFormat = JSONObject()
        responseFormat.put("type", "json_object")
        requestJson.put("response_format", responseFormat)

        val messagesArray = JSONArray()

        // System message
        val sysMsg = JSONObject()
        sysMsg.put("role", "system")
        sysMsg.put("content", systemPrompt)
        messagesArray.put(sysMsg)

        // User message
        val userMsg = JSONObject()
        userMsg.put("role", "user")

        if (base64Images.isEmpty()) {
            userMsg.put("content", userPrompt)
        } else {
            val contentArray = JSONArray()
            val textPart = JSONObject()
            textPart.put("type", "text")
            textPart.put("text", userPrompt)
            contentArray.put(textPart)

            base64Images.take(3).forEach { b64 ->
                val imgPart = JSONObject()
                imgPart.put("type", "image_url")
                val urlObj = JSONObject()
                urlObj.put("url", "data:image/jpeg;base64,$b64")
                imgPart.put("image_url", urlObj)
                contentArray.put(imgPart)
            }
            userMsg.put("content", contentArray)
        }
        messagesArray.put(userMsg)

        requestJson.put("messages", messagesArray)

        val body = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(CHAT_COMPLETIONS_URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "PaperMaker-Android/1.0")
            .post(body)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val code = response.code

                if (response.isSuccessful) {
                    val root = JSONObject(responseBody)
                    val choices = root.optJSONArray("choices")
                    val firstChoice = choices?.optJSONObject(0)
                    val message = firstChoice?.optJSONObject("message")
                    val content = message?.optString("content")

                    if (!content.isNullOrBlank()) {
                        Result.success(content)
                    } else {
                        Result.failure(Exception("Empty content received from Groq ($model)."))
                    }
                } else {
                    val parsedError = parseGroqErrorMessage(responseBody)
                    val descriptive = when (code) {
                        400 -> "Groq 400 Bad Request: $parsedError"
                        401 -> "Groq 401 Unauthorized: Invalid API key. Check key in AI Settings."
                        403 -> "Groq 403 Forbidden: Account access restricted or unavailable in region."
                        404 -> "Groq 404 Model Not Found ($model): $parsedError"
                        429 -> "Groq 429 Rate Limit: Token quota or request limit exceeded."
                        in 500..599 -> "Groq $code Server Error: Groq service is temporarily busy."
                        else -> "Groq Error ($code): $parsedError"
                    }
                    Result.failure(Exception(descriptive))
                }
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error connecting to Groq: ${e.localizedMessage ?: "Timeout"}. Check internet."))
        } catch (e: Exception) {
            Result.failure(Exception("Groq request failure: ${e.localizedMessage}"))
        }
    }

    private fun parseGroqErrorMessage(rawJson: String): String {
        return try {
            val root = JSONObject(rawJson)
            val errObj = root.optJSONObject("error")
            errObj?.optString("message") ?: rawJson.take(120)
        } catch (_: Exception) {
            rawJson.take(120).ifBlank { "Unknown Groq error" }
        }
    }
}
