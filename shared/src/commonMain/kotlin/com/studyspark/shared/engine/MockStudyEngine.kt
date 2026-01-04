package com.studyspark.shared.engine

import com.studyspark.shared.model.*

class MockStudyEngine : StudyEngine {
    override suspend fun process(request: StudyRequest): StudyResponse {
        // 1. Validation
        if (request.input.isBlank()) {
            throw IllegalArgumentException("Input cannot be empty. Please enter a topic or text.")
        }

        val cleanInput = request.input.trim()
        val difficultyPrefix = when (request.difficulty) {
            Difficulty.BEGINNER -> "Simply put,"
            Difficulty.INTERMEDIATE -> "To explain,"
            Difficulty.ADVANCED -> "Technically speaking,"
        }
        
        val styleSuffix = if (request.learningStyle == LearningStyle.SHORT) "(Brief Overview)" else "(Detailed Analysis)"
        val variationTag = if (request.variation == Variation.REPHRASED) "[Rephrased]" else ""

        // 2. Mock Logic
        val title = if (request.mode == StudyMode.TOPIC) "Topic: $cleanInput" else "Text Analysis"
        
        val explanation = """
            $variationTag $title $styleSuffix
            $difficultyPrefix $cleanInput is a fascinating subject. 
            This is a generated explanation to demonstrate the shared logic layout.
            (Imagine a real AI explaining this concept adapting to ${request.difficulty} level).
        """.trimIndent()

        val summary = if (request.mode == StudyMode.TEXT) {
            "Summary: You pasted text about '${cleanInput.take(20)}...'. Here is the key takeaway."
        } else null

        val quizQuestions = listOf(
            "What is the main concept of $cleanInput?",
            "How does $cleanInput relate to ${request.difficulty} concepts?",
            "True or False: $cleanInput is essential for studying."
        )

        val flashcards = listOf(
            Flashcard(question = "Define $cleanInput", answer = "It is $cleanInput"),
            Flashcard(question = "Key feature of $cleanInput?", answer = "It is versatile.")
        )
        
        val motivation = "Keep going! Learning $cleanInput is making you smarter every second."

        return StudyResponse(
            explanation = explanation,
            summary = summary,
            quizQuestions = quizQuestions,
            flashcards = flashcards,
            motivationQuote = motivation
        )
    }
}
