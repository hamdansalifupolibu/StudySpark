package com.studyspark.shared

actual fun getPlatformApiKey(): String? {
    return try {
        System.getenv("GEMINI_API_KEY")
    } catch (e: Exception) {
        null
    }
}
