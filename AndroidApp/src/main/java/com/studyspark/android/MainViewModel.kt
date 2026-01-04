package com.studyspark.android

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyspark.shared.engine.GeminiStudyEngine
import com.studyspark.shared.engine.MockStudyEngine
import com.studyspark.shared.engine.StudyEngine
import com.studyspark.shared.model.Difficulty
import com.studyspark.shared.model.LearningStyle
import com.studyspark.shared.model.StudyMode
import com.studyspark.shared.model.StudyRequest
import com.studyspark.shared.model.StudyResponse
import com.studyspark.shared.model.Variation
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Idle : UiState
    data object Loading : UiState
    data class Success(val response: StudyResponse) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel : ViewModel() {
    // State
    var uiState: UiState by mutableStateOf(UiState.Idle)
        private set
        
    var input by mutableStateOf("")
    var selectedMode by mutableStateOf(StudyMode.TOPIC)
    var selectedDifficulty by mutableStateOf(Difficulty.INTERMEDIATE)
    var selectedStyle by mutableStateOf(LearningStyle.SHORT)

    // Engine
    // NOTE: In a real app, inject this. For this contest demo, we instantiate directly.
    private val apiKey = "AIzaSyARsMd7qVbekiVn0uXh3ASwM7y4QZ92tQY" // Demo key from WebApp fallback
    private val engine: StudyEngine = if (apiKey.isNotBlank()) GeminiStudyEngine(apiKey) else MockStudyEngine()

    fun generateStudyAid(variation: Variation = Variation.ORIGINAL) {
        if (input.isBlank()) return
        
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                val request = StudyRequest(
                    mode = selectedMode,
                    input = input,
                    difficulty = selectedDifficulty,
                    learningStyle = selectedStyle,
                    variation = variation
                )
                val response = engine.process(request)
                uiState = UiState.Success(response)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
