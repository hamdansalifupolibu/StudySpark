package com.studyspark.shared.engine

import com.studyspark.shared.model.StudyRequest
import com.studyspark.shared.model.StudyResponse

interface StudyEngine {
    @Throws(Exception::class) // Define that this can throw validation exceptions
    suspend fun process(request: StudyRequest): StudyResponse
}
