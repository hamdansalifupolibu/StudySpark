package com.studyspark.shared.model

data class Flashcard(
    val question: String,
    val answer: String
)

data class StudyRequest(
    val mode: StudyMode,
    val input: String,
    val difficulty: Difficulty,
    val learningStyle: LearningStyle,
    val variation: Variation
)

data class StudyResponse(
    val explanation: String,
    val summary: String?, // Nullable as it might not be applicable for TOPIC mode or can be optional
    val quizQuestions: List<String>,
    val flashcards: List<Flashcard>,
    val motivationQuote: String
)
