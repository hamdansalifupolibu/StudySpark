package com.studyspark.shared.engine

import com.studyspark.shared.model.Flashcard
import com.studyspark.shared.model.StudyResponse

object ResponseParser {
    fun parse(rawText: String): StudyResponse {
        var explanation = ""
        var summary: String? = null
        val quiz = mutableListOf<String>()
        val flashcards = mutableListOf<Flashcard>()
        var motivation = "Keep learning!"
        
        // Simple state machine parser or Regex split
        // Let's use split by sections first
        
        val lines = rawText.lines()
        var currentSection = ""
        var currentBuffer = StringBuilder()
        
        // Temporary flashcard buffer
        var currentQ = ""
        var currentA = ""

        fun commitSection() {
            val content = currentBuffer.toString().trim()
            if (content.isEmpty()) return
            
            when (currentSection) {
                "EXPLANATION:" -> explanation = content
                "SUMMARY:" -> summary = if (content.equals("EMPTY", ignoreCase = true)) null else content
                "MOTIVATION:" -> motivation = content
                "QUIZ:" -> {
                    content.lines().forEach { line ->
                        if (line.trim().startsWith("-")) {
                            quiz.add(line.trim().removePrefix("-").trim())
                        }
                    }
                }
                "FLASHCARDS:" -> {
                    // Flashcards are a bit trickier, handled inside loop usually or post-process
                }
            }
            currentBuffer.clear()
        }

        for (line in lines) {
            val trimLine = line.trim()
            when {
                trimLine == "EXPLANATION:" || trimLine == "SUMMARY:" || 
                trimLine == "QUIZ:" || trimLine == "FLASHCARDS:" || 
                trimLine == "MOTIVATION:" -> {
                    commitSection()
                    currentSection = trimLine
                }
                currentSection == "FLASHCARDS:" -> {
                    if (trimLine.startsWith("Q:")) {
                        if (currentQ.isNotEmpty() && currentA.isNotEmpty()) {
                            flashcards.add(Flashcard(currentQ, currentA))
                            currentQ = ""
                            currentA = ""
                        }
                        currentQ = trimLine.removePrefix("Q:").trim()
                    } else if (trimLine.startsWith("A:")) {
                        currentA = trimLine.removePrefix("A:").trim()
                    }
                }
                else -> {
                    if (currentSection != "FLASHCARDS:") {
                        currentBuffer.append(line).append("\n")
                    }
                }
            }
        }
        commitSection() // Commit last section
        
        // Final flashcard flush
        if (currentQ.isNotEmpty() && currentA.isNotEmpty()) {
             flashcards.add(Flashcard(currentQ, currentA))
        }

        return StudyResponse(
            explanation = explanation.ifBlank { "Could not generate explanation." },
            summary = summary,
            quizQuestions = quiz,
            flashcards = flashcards,
            motivationQuote = motivation
        )
    }
}
