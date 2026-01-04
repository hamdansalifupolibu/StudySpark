import com.studyspark.shared.engine.GeminiStudyEngine
import com.studyspark.shared.engine.MockStudyEngine
import com.studyspark.shared.engine.StudyEngine
import com.studyspark.shared.model.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement

fun main() {
    // SECURITY WARNING: In a real production app, you would proxy this request or use a secure way to hold keys.
    // For this local contest demo, prompt the user or use a dev key.
    // We will check if the user provided ?key=XYZ in URL, otherwise default to a placeholder or prompt.
    val urlParams = org.w3c.dom.url.URLSearchParams(kotlinx.browser.window.location.search)
    // Priority: 1. URL Param, 2. LocalStorage, 3. Hardcoded Default (for contest demo)
    var apiKey = urlParams.get("key")
    
    if (apiKey.isNullOrBlank()) {
         apiKey = kotlinx.browser.localStorage.getItem("gemini_key")
    }
    
    // Fallback to the provided demo key if still null
    if (apiKey.isNullOrBlank()) {
        apiKey = "AIzaSyARsMd7qVbekiVn0uXh3ASwM7y4QZ92tQY"
    }
    
    // Save it back to storage to keep things sync
    if (!apiKey.isNullOrBlank()) {
        kotlinx.browser.localStorage.setItem("gemini_key", apiKey)
    }
    
    // Default to Real Engine now that we have a key
    val engine: StudyEngine = if (!apiKey.isNullOrBlank()) {
        GeminiStudyEngine(apiKey)
    } else {
        MockStudyEngine()
    }
    
    val scope = MainScope()

    kotlinx.browser.window.addEventListener("load", {
        // Target the root div defined in index.html
        val root = document.getElementById("root") as? HTMLDivElement ?: return@addEventListener
        root.innerHTML = "" // Clear the "Welcome" message
        
        // Show warning if Mock mode
        if (apiKey.isNullOrBlank()) {
             val warning = document.createElement("div").apply {
                className = "warning-box"
                innerHTML = "<i class='fas fa-exclamation-triangle'></i> <div><strong>Mock Mode Active.</strong>&nbsp;To use Real AI, add&nbsp;<code>?key=YOUR_KEY</code>&nbsp;to the URL.</div>"
             }
             root.appendChild(warning)
        }
        
        // --- UI CONSTRUCTION ---
        
        val motivationDiv = document.createElement("div").apply { 
            id = "motivation"
            className = "motivation-box"
            textContent = "Loading motivation..." 
        }

        // 2. Controls - Mode
        val modeSelect = createSelect("Mode", listOf("TOPIC", "TEXT"))

        // 3. Input
        val inputField = (document.createElement("input") as HTMLInputElement).apply {
            placeholder = "Enter topic..."
            type = "text"
        }
        
        // 4. Controls - Settings
        val diffSelect = createSelect("Difficulty", listOf("BEGINNER", "INTERMEDIATE", "ADVANCED"))
        val styleSelect = createSelect("Style", listOf("SHORT", "DETAILED"))
        
        // 5. Actions
        val explainBtn = createButton("<i class='fas fa-lightbulb'></i> Explain / Study")
        val flashcardBtn = createButton("<i class='fas fa-clone'></i> Generate Flashcards")
        val rephraseBtn = createButton("<i class='fas fa-pen-fancy'></i> Rephrase")
        rephraseBtn.className = "secondary"

        // 6. Output Area
        val outputDiv = document.createElement("div").apply { 
            id = "output" 
            className = "output-area"
        }

        // --- LAYOUT ---
        root.appendChild(motivationDiv)
        
        // Input Group
        val inputGroup = document.createElement("div").apply { className = "input-group" }
        inputGroup.appendChild(createLabel("What do you want to study?"))
        inputGroup.appendChild(inputField)
        root.appendChild(inputGroup)
        root.appendChild(document.createElement("br"))

        // Controls Grid
        val grid = document.createElement("div").apply { className = "controls-grid" }
        
        // Helper to wrap control with label
        fun addControl(label: String, element: org.w3c.dom.Element) {
            val wrapper = document.createElement("div").apply { className = "input-group" }
            wrapper.appendChild(createLabel(label))
            wrapper.appendChild(element)
            grid.appendChild(wrapper)
        }
        
        addControl("Mode", modeSelect)
        addControl("Difficulty", diffSelect)
        addControl("Style", styleSelect)
        
        root.appendChild(grid)

        // Action Buttons
        val btnGroup = document.createElement("div").apply { className = "button-group" }
        btnGroup.appendChild(explainBtn)
        btnGroup.appendChild(flashcardBtn)
        btnGroup.appendChild(rephraseBtn)
        root.appendChild(btnGroup)

        root.appendChild(outputDiv)

        // --- LOGIC ---
        
        // Initial Motivation
        val quotes = listOf(
            "\"The roots of education are bitter, but the fruit is sweet.\" - Aristotle",
            "\"Education is the passport to the future, for tomorrow belongs to those who prepare for it today.\" - Malcolm X",
            "\"The only true wisdom is in knowing you know nothing.\" - Socrates",
            "\"I cannot teach anybody anything. I can only make them think.\" - Socrates",
            "\"Education is not the filling of a pail, but the lighting of a fire.\" - William Butler Yeats",
            "\"Live as if you were to die tomorrow. Learn as if you were to live forever.\" - Mahatma Gandhi",
            "\"The mind is not a vessel to be filled, but a fire to be kindled.\" - Plutarch",
            "\"Knowledge is power.\" - Francis Bacon"
        )
        motivationDiv.textContent = "✨ ${quotes.random()}"

        fun getCommonRequest(variation: Variation): StudyRequest {
            return StudyRequest(
                mode = StudyMode.valueOf(modeSelect.value),
                input = inputField.value,
                difficulty = Difficulty.valueOf(diffSelect.value),
                learningStyle = LearningStyle.valueOf(styleSelect.value),
                variation = variation
            )
        }

        fun renderResponse(response: StudyResponse) {
            motivationDiv.textContent = "✨ ${response.motivationQuote}"
            
            val sb = StringBuilder()
            
            // Explanation Card
            sb.append("<div class='result-card'>")
            sb.append("<h2><i class='fas fa-book-open'></i> Explain / Study</h2>")
            sb.append("<div>${response.explanation.replace("\n", "<br>")}</div>")
            sb.append("</div>")
            
            if (response.summary != null) {
                sb.append("<div class='result-card'>")
                sb.append("<h2><i class='fas fa-list-alt'></i> Summary</h2>")
                sb.append("<p>${response.summary}</p>")
                sb.append("</div>")
            }
            
            if (response.quizQuestions.isNotEmpty()) {
                sb.append("<div class='result-card'>")
                sb.append("<h2><i class='fas fa-question-circle'></i> Quiz</h2><ul>")
                response.quizQuestions.forEach { q -> sb.append("<li>$q</li>") }
                sb.append("</ul></div>")
            }

            if (response.flashcards.isNotEmpty()) {
                 sb.append("<div class='result-card'>")
                sb.append("<h2><i class='fas fa-clone'></i> Flashcards</h2>")
                response.flashcards.forEach { card ->
                    sb.append("""
                        <details class="flashcard">
                            <summary><i class="fas fa-question-circle"></i> Q: ${card.question}</summary>
                            <div class="flashcard-content">
                                <strong>A:</strong> ${card.answer}
                            </div>
                        </details>
                    """.trimIndent())
                }
                 sb.append("</div>")
            }
            outputDiv.innerHTML = sb.toString()
        }

        fun handleRequest(variation: Variation) {
            scope.launch {
                try {
                    outputDiv.textContent = "Simply thinking..."
                    // A little fake loading pulse could be nice here, but text is fine.
                    outputDiv.innerHTML = "<div style='text-align:center; padding:20px; color:#666;'>🧠 BRAIN POWER ACTIVATING...</div>"
                    
                    val request = getCommonRequest(variation)
                    val response = engine.process(request)
                    renderResponse(response)
                } catch (e: Exception) {
                    outputDiv.innerHTML = "<div class='warning-box' style='color:red;'>Error: ${e.message}</div>"
                }
            }
        }

        // Listeners
        explainBtn.onclick = { handleRequest(Variation.ORIGINAL) }
        flashcardBtn.onclick = { handleRequest(Variation.ORIGINAL) } 
        rephraseBtn.onclick = { handleRequest(Variation.REPHRASED) }
        
        modeSelect.addEventListener("change", { 
            inputField.placeholder = if (modeSelect.value == "TEXT") "Paste your study notes here..." else "Enter topic (e.g. Kotlin Coroutines)..."
        })
    })
}

// Helpers
fun createButton(html: String): HTMLButtonElement {
    return (document.createElement("button") as HTMLButtonElement).apply {
        innerHTML = html
        setAttribute("style", "padding: 8px 16px; cursor: pointer;")
    }
}

fun createSelect(label: String, options: List<String>): HTMLSelectElement {
    val select = document.createElement("select") as HTMLSelectElement
    options.forEach { opt ->
        val el = document.createElement("option") as HTMLOptionElement
        el.value = opt
        el.text = opt
        select.appendChild(el)
    }
    return select
}

fun createLabel(text: String): org.w3c.dom.Element {
    return document.createElement("label").apply { textContent = text }
}
