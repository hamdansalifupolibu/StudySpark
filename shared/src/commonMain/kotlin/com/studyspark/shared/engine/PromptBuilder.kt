package com.studyspark.shared.engine

import com.studyspark.shared.model.*

object PromptBuilder {
    fun build(request: StudyRequest): String {
        val topicOrText = request.input
        val difficulty = when (request.difficulty) {
            Difficulty.BEGINNER -> "Explain like I'm a beginner (simple 101 level)."
            Difficulty.INTERMEDIATE -> "Explain at an intermediate level (undergrad standard)."
            Difficulty.ADVANCED -> "Explain at an advanced expert level (technical deep dive)."
        }
        val style = when (request.learningStyle) {
            LearningStyle.SHORT -> "Keep the explanation concise and short."
            LearningStyle.DETAILED -> "Provide a detailed and comprehensive explanation."
        }
        val variation = if (request.variation == Variation.REPHRASED) "Please REPHRASE this explanation differently than usual." else ""

        val context = if (request.mode == StudyMode.TOPIC) {
            "The user wants to learn about the TOPIC: '$topicOrText'"
        } else {
            "The user has provided the following study NOTES/TEXT: \n'$topicOrText'\n. SUMMARIZE and explain this text."
        }

        return """
            You are StudySpark, an AI Study Companion.
            $context
            
            CONFIGURATION:
            - Difficulty: $difficulty
            - Style: $style
            - Variation: $variation
            
            REQUIRED OUTPUT FORMAT:
            You must output your response in the EXACT format below. Do not use markdown blocks unless specified.
            
            EXPLANATION:
            <The main explanation text goes here>

            SUMMARY:
            <If mode is TEXT, put summary here. If mode is TOPIC, leave empty or quick summary>

            QUIZ:
            - <Question 1>
            - <Question 2>
            - <Question 3>

            FLASHCARDS:
            Q: <Front of card 1>
            A: <Back of card 1>

            Q: <Front of card 2>
            A: <Back of card 2>

            MOTIVATION:
            <A short inspiring quote from a famous educationist, philosopher, or thinker, including their name>
        """.trimIndent()
    }
}
