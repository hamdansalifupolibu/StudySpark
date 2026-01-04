package com.studyspark.shared.engine

import com.studyspark.shared.getPlatformApiKey

object StudyEngineFactory {
    /**
     * Creates a StudyEngine based on available configuration.
     * Order of precedence:
     * 1. Environment Variable (GEMINI_API_KEY) - via getPlatformApiKey()
     * 2. Web URL Parameter (passed explicitly)
     * 3. Mock Mode (Default)
     */
    fun create(webUrlKey: String? = null): StudyEngine {
        val envKey = getPlatformApiKey()
        // Priority: Env -> URL -> Mock
        val key = envKey?.takeIf { it.isNotBlank() } ?: webUrlKey?.takeIf { it.isNotBlank() }
        
        return if (key != null) {
            println("StudySpark: Initializing Gemini Engine with provided key.")
            GeminiStudyEngine(key)
        } else {
            println("StudySpark: No API Key found (Checked Env: GEMINI_API_KEY & URL Param). Using Mock Mode.")
            MockStudyEngine()
        }
    }
}
