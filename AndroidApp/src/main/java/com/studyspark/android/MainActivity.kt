package com.studyspark.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyspark.shared.model.Difficulty
import com.studyspark.shared.model.Flashcard
import com.studyspark.shared.model.LearningStyle
import com.studyspark.shared.model.StudyMode
import com.studyspark.shared.model.StudyResponse
import com.studyspark.shared.model.Variation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Web Palette
            val primaryColor = Color(0xFF6366f1)
            val secondaryColor = Color(0xFFec4899)
            val backgroundColor = Color(0xFFF8FAFC) // Fallback

            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = primaryColor,
                    secondary = secondaryColor,
                    background = backgroundColor,
                    surface = Color.White,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onSurface = Color(0xFF1e293b) // Slate 800
                )
            ) {
                // Main Gradient Background Wrapper
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667eea), // Indigo Light
                                    Color(0xFF764ba2)  // Deep Purple
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(1000f, 1000f)
                            )
                        )
                ) {
                    StudyScreen()
                }
            }
        }
    }
}

@Composable
fun StudyScreen(viewModel: MainViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    var showAbout by remember { mutableStateOf(false) }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "StudySpark ⚡",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color(0xFF6366f1),
                            offset = Offset(0f, 4f),
                            blurRadius = 15f
                        )
                    )
                )
                Text(
                    text = "AI-Powered Study Assistant",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Glassy Info Button
            IconButton(
                onClick = { showAbout = true },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Text("ℹ️", fontSize = 20.sp)
            }
        }

        // GLASS CONTAINER FOR INPUTS
        GlassCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 // INPUT
                OutlinedTextField(
                    value = viewModel.input,
                    onValueChange = { viewModel.input = it },
                    label = { Text("What do you want to study?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = viewModel.selectedMode == StudyMode.TOPIC,
                    minLines = if (viewModel.selectedMode == StudyMode.TEXT) 3 else 1,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = Color.White.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                // MODE SELECTOR
                StudyDropdown(
                    label = "Mode",
                    options = StudyMode.values().map { it.name },
                    selected = viewModel.selectedMode.name,
                    onSelected = { viewModel.selectedMode = StudyMode.valueOf(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // DIFFICULTY & STYLE ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudyDropdown(
                        label = "Difficulty",
                        options = Difficulty.values().map { it.name },
                        selected = viewModel.selectedDifficulty.name,
                        onSelected = { viewModel.selectedDifficulty = Difficulty.valueOf(it) },
                        modifier = Modifier.weight(1f)
                    )

                    StudyDropdown(
                        label = "Style",
                        options = LearningStyle.values().map { it.name },
                        selected = viewModel.selectedStyle.name,
                        onSelected = { viewModel.selectedStyle = LearningStyle.valueOf(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // ACTION BUTTONS
                val isLoading = viewModel.uiState is UiState.Loading
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.generateStudyAid(Variation.ORIGINAL) },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Explain 🚀", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.generateStudyAid(Variation.REPHRASED) },
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Rephrase 🔄", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // OUTPUT AREA
        when (val state = viewModel.uiState) {
            is UiState.Idle -> {
                GlassCard {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ready to learn!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Enter a topic above to get started.",
                            color = Color.Gray
                        )
                    }
                }
            }
            is UiState.Loading -> {
                GlassCard {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Consulting the AI... 🧠",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            is UiState.Error -> {
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("❌ Error", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            is UiState.Success -> {
                ResultDisplay(state.response)
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // Rounded XL (16dp approx)
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f) // High opacity for readability
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
fun SectionHeader(title: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ResultDisplay(response: StudyResponse) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        
        // Motivation
        GlassCard {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✨", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = response.motivationQuote,
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Explanation
        GlassCard {
            Column(modifier = Modifier.padding(20.dp)) {
                SectionHeader("Explanation", "📖")
                Text(
                    text = response.explanation,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = Color(0xFF334155) // Slate 700
                )
            }
        }
        
        // Summary
        response.summary?.let { summary ->
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    SectionHeader("Summary", "📝")
                    Text(summary, color = Color(0xFF334155))
                }
            }
        }
        
        // Quiz
        if (response.quizQuestions.isNotEmpty()) {
             GlassCard {
                 Column(modifier = Modifier.padding(20.dp)) {
                    SectionHeader("Quiz", "❓")
                    response.quizQuestions.forEach { q ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(q, color = Color(0xFF334155))
                        }
                    }
                }
            }
        }
        
        // Flashcards
        if (response.flashcards.isNotEmpty()) {
             GlassCard {
                 Column(modifier = Modifier.padding(20.dp)) {
                    SectionHeader("Flashcards", "🃏")
                    Text(
                        "Tap cards to flip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    response.flashcards.forEach { card ->
                        FlashcardItem(card)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp)) // Bottom padding
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardItem(flashcard: Flashcard) {
    var isFlipped by remember { mutableStateOf(false) }
    
    Card(
        onClick = { isFlipped = !isFlipped },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = flashcard.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (isFlipped) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = flashcard.answer,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF334155)
                    )
                }
            } else {
                 Spacer(modifier = Modifier.height(12.dp))
                 Text(
                    text = "Tap to reveal answer...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("StudySpark ⚡", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "StudySpark is an AI-powered study companion demonstrating Kotlin Multiplatform capabilities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF334155)
                )
                
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                         Text("ARCHITECTURE NOTE:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF64748B))
                         Text(
                             "This Android Ul is 100% Jetpack Compose. All interactions are handled by the shared KMP module, even the AI system.", 
                             fontSize = 12.sp,
                             color = Color(0xFF334155)
                         )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Created by Hamdan Salifu Polibu",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1e293b)
                )
                Text("University of Mines and Technology (UMaT)", fontSize = 12.sp, color = Color.Gray)
                Text("ce-hspolibu8923@st.umat.edu.gh", fontSize = 12.sp, color = Color(0xFF6366f1))
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366f1))
            ) {
                Text("Awesome!")
            }
        }
    )
}
