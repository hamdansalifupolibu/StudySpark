package com.studyspark.shared.engine

import com.studyspark.shared.model.StudyRequest
import com.studyspark.shared.model.StudyResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Gemini Request/Response Models
@Serializable
data class GeminiRequest(val contents: List<Content>)

@Serializable
data class Content(val parts: List<Part>)

@Serializable
data class Part(val text: String)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
data class GeminiError(
    val code: Int,
    val message: String,
    val status: String
)

@Serializable
data class Candidate(val content: Content)

class GeminiStudyEngine(private val apiKey: String) : StudyEngine {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { 
                ignoreUnknownKeys = true 
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun process(request: StudyRequest): StudyResponse {
        if (request.input.isBlank()) throw IllegalArgumentException("Input cannot be empty")

        val prompt = PromptBuilder.build(request)
        
        try {
            // Using Gemini Flash Latest (Alias for newest stable flash model)
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=$apiKey") {
                contentType(io.ktor.http.ContentType.Application.Json)
                setBody(GeminiRequest(listOf(Content(listOf(Part(prompt))))))
            }.body()

            if (response.error != null) {
                throw Exception("API Error: ${response.error.message}")
            }

            val textOutput = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: throw Exception("Empty response from AI (No candidates returned)")

            return ResponseParser.parse(textOutput)

        } catch (e: Exception) {
            // Fallback / Error Handling
            return StudyResponse(
                explanation = "Error communicating with AI: ${e.message}",
                summary = null,
                quizQuestions = listOf("Error generating quiz."),
                flashcards = emptyList(),
                motivationQuote = "Don't give up!"
            )
        }
    }
}
