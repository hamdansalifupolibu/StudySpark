plugins {
    // Applying Kotlin Multiplatform plugin to root to ensure version consistency
    kotlin("multiplatform").version("1.9.21").apply(false)
    kotlin("android").version("1.9.21").apply(false)
    id("com.android.application") version "8.2.2" apply false
}

group = "com.studyspark"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
