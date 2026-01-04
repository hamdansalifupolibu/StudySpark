package com.studyspark.shared

actual fun getPlatformApiKey(): String? {
    return null // Environment variables are not directly accessible in standard JS build without configuration. We use URL params in Main.kt.
}
