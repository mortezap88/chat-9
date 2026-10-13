package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VibrationPatterns
import com.example.audio.VoicePersonas
import com.example.data.ApiKeyItem
import com.example.data.AppSettings
import com.example.data.CustomPersona
import com.example.data.KeyStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SettingsCategory(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    CHAT_DISPLAY(
        title = "Chat & Display",
        description = "Token streaming, font scaling & display options",
        icon = Icons.AutoMirrored.Filled.Chat
    ),
    MODELS_REASONING(
        title = "AI Models & Thinking",
        description = "Primary Gemini tiers & calibrated reasoning depth",
        icon = Icons.Default.Tune
    ),
    LANGUAGE_INSPECTOR(
        title = "Language & Text Inspector",
        description = "Contextual smoothing and interactive grammar tap-to-fix",
        icon = Icons.Default.Spellcheck
    ),
    AI_PERSONA_VOICE(
        title = "AI Persona & Voice",
        description = "Grounded human characters, 30 Gemini voices & persona studio",
        icon = Icons.Default.Person
    ),
    VOICE_INPUT(
        title = "Voice & Microphone",
        description = "Gemini 3.5 audio engine, sensitivity & haptic patterns",
        icon = Icons.Default.Mic
    ),
    LONG_TERM_MEMORY(
        title = "Long-Term Memory",
        description = "Memory vault notes & Hybrid Vector RAG parameters",
        icon = Icons.Default.Psychology
    ),
    API_KEYS(
        title = "API Keys & Pool",
        description = "Failover key pool, add keys & bulk configuration",
        icon = Icons.Default.Key
    ),
    WEB_SEARCH(
        title = "Web Search & Sources",
        description = "Search thoroughness depth & max sources limit",
        icon = Icons.Default.Public
    )
}

@Composable
fun SettingsScreen(
    currentSettings: AppSettings,
    apiKeys: List<ApiKeyItem>,
    tavilyApiKeys: List<ApiKeyItem> = emptyList(),
    customPersonas: List<CustomPersona> = emptyList(),
    memoryCount: Int = 0,
    onOpenMemoryVault: () -> Unit = {},
    onSaveSettings: (AppSettings) -> Unit,
    onSaveCustomPersona: (CustomPersona) -> Unit = {},
    onDeleteCustomPersona: (String) -> Unit = {},
    onAddKey: (String) -> Unit,
    onBulkAddKeys: (List<String>) -> Unit,
    onDeleteKey: (String) -> Unit,
    onAddTavilyKey: (String) -> Unit = {},
    onBulkAddTavilyKeys: (List<String>) -> Unit = {},
    onDeleteTavilyKey: (String) -> Unit = {},
    onResetDefaults: () -> Unit,
    onPreviewVibration: ((String) -> Unit)? = null,
    onStopPreviewVibration: (() -> Unit)? = null,
    onPreviewVoicePersona: ((String) -> Unit)? = null,
    onStopPreviewVoicePersona: (() -> Unit)? = null,
    currentlySpeakingPersonaId: String? = null,
    isLoadingPersonaId: String? = null,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }

    var primaryModel by remember { mutableStateOf(currentSettings.primaryModel) }
    var thinkingEnabled by remember { mutableStateOf(currentSettings.thinkingEnabled) }
    var thinkingLevel by remember { mutableStateOf(currentSettings.thinkingLevel) }
    var tokenStreaming by remember { mutableStateOf(currentSettings.tokenStreaming) }
    var contextualSmoothing by remember { mutableStateOf(currentSettings.contextualSmoothing) }
    var grammarInspector by remember { mutableStateOf(currentSettings.grammarInspector) }
    var micSensitivity by remember { mutableStateOf(currentSettings.micSensitivity) }
    var silencePauseDuration by remember { mutableFloatStateOf(currentSettings.silencePauseDuration) }
    var minVoiceHoldDurationSeconds by remember { mutableFloatStateOf(currentSettings.minVoiceHoldDurationSeconds) }
    var selectedVibrationPattern by remember { mutableStateOf(currentSettings.vibrationPattern) }
    var previewingPatternId by remember { mutableStateOf<String?>(null) }
    var maxFontSizeSp by remember { mutableIntStateOf(currentSettings.maxFontSizeSp) }
    var longTermMemoryEnabled by remember { mutableStateOf(currentSettings.longTermMemoryEnabled) }
    var maxRetrievedMemories by remember { mutableIntStateOf(currentSettings.maxRetrievedMemories) }
    var selectedVoicePersona by remember { mutableStateOf(currentSettings.selectedVoicePersona) }
    var tavilyThoroughness by remember { mutableStateOf(currentSettings.tavilyThoroughness) }
    var tavilyMaxResults by remember { mutableIntStateOf(currentSettings.tavilyMaxResults) }

    // Persona Filter States
    var personaGenderFilter by remember { mutableStateOf("All") }
    var personaRegionFilter by remember { mutableStateOf("All") }
    var showCreatePersonaDialog by remember { mutableStateOf(false) }

    var newKeyInput by remember { mutableStateOf("") }
    var newTavilyKeyInput by remember { mutableStateOf("") }
    var apiKeyTab by remember { mutableIntStateOf(0) }

    fun syncAndSave() {
        onSaveSettings(
            currentSettings.copy(
                primaryModel = primaryModel,
                thinkingEnabled = thinkingEnabled,
                thinkingLevel = thinkingLevel,
                tokenStreaming = tokenStreaming,
                contextualSmoothing = contextualSmoothing,
                grammarInspector = grammarInspector,
                micSensitivity = micSensitivity,
                silencePauseDuration = silencePauseDuration,
                vibrationPattern = selectedVibrationPattern,
                maxFontSizeSp = maxFontSizeSp,
                longTermMemoryEnabled = longTermMemoryEnabled,
                maxRetrievedMemories = maxRetrievedMemories,
                minVoiceHoldDurationSeconds = minVoiceHoldDurationSeconds,
                selectedVoicePersona = selectedVoicePersona,
                tavilyThoroughness = tavilyThoroughness,
                tavilyMaxResults = tavilyMaxResults
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedCategory != null) {
                IconButton(onClick = { selectedCategory = null }, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF18181B))
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedCategory?.title ?: "Settings",
                    color = Color(0xFF18181B),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (selectedCategory != null) "Customize selected options" else "Select a category to customize",
                    color = Color(0xFF71717A),
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onClose, modifier = Modifier.size(36.dp).testTag("close_settings_button")) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF18181B))
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE4E4E7)))

        AnimatedContent(
            targetState = selectedCategory,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "CategoryTransition",
            modifier = Modifier.weight(1f)
        ) { category ->
            if (category == null) {
                // Hub View
                val hubScroll = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(hubScroll).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "PREFERENCES & OPTIONS", color = Color(0xFF71717A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    SettingsCategory.entries.forEach { cat ->
                        CategoryHubCard(category = cat, onClick = { selectedCategory = cat })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFD4D4D8), RoundedCornerShape(12.dp))
                            .clickable {
                                onResetDefaults()
                                primaryModel = "gemini-flash-latest"
                                thinkingEnabled = false
                                thinkingLevel = "Low"
                                tokenStreaming = true
                                contextualSmoothing = true
                                grammarInspector = true
                                micSensitivity = "Low"
                                silencePauseDuration = 2.0f
                                maxFontSizeSp = 24
                                longTermMemoryEnabled = true
                                maxRetrievedMemories = 4
                                syncAndSave()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset All Settings to Defaults", color = Color(0xFF3F3F46), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                val detailScroll = rememberScrollState()
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(detailScroll).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (category) {
                        SettingsCategory.CHAT_DISPLAY -> {
                            MonochromeToggle(
                                title = "Token Streaming",
                                description = "Stream response tokens in real-time as they generate for fast visual emergence.",
                                checked = tokenStreaming,
                                onCheckedChange = { tokenStreaming = it; syncAndSave() },
                                testTag = "toggle_token_streaming"
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).padding(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Maximum Font Scale", color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("${maxFontSizeSp}sp", color = Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Auto-shrinks dynamic typography smoothly to keep complete thoughts in view.", color = Color(0xFF71717A), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp))
                                Slider(
                                    value = maxFontSizeSp.toFloat(),
                                    onValueChange = { maxFontSizeSp = it.toInt(); syncAndSave() },
                                    valueRange = 18f..48f,
                                    steps = 14,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF18181B), activeTrackColor = Color(0xFF18181B), inactiveTrackColor = Color(0xFFD4D4D8))
                                )
                            }
                        }

                        SettingsCategory.MODELS_REASONING -> {
                            Text("Primary Chat Model", color = Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val modelChoices = listOf(
                                Pair("Gemini Flash Latest (Gemini 3.8 Flash)", "gemini-flash-latest"),
                                Pair("Gemini Pro Latest (Gemini 3.1 Pro)", "gemini-pro-latest"),
                                Pair("Gemini Flash-Lite Latest (Gemini 3.5 Flash-Lite)", "gemini-flash-lite-latest")
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                modelChoices.forEach { (label, modelKey) ->
                                    val isSelected = primaryModel == modelKey
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isSelected) Color(0xFF18181B) else Color(0xFFF4F4F5)).border(1.dp, if (isSelected) Color(0xFF18181B) else Color(0xFFE4E4E7), RoundedCornerShape(10.dp)).clickable { primaryModel = modelKey; syncAndSave() }.padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(label, color = if (isSelected) Color.White else Color(0xFF3F3F46), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            if (isSelected) Text("ACTIVE", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            MonochromeToggle(
                                title = "Thinking Mode",
                                description = "Enables internal reasoning steps before generating the answer.",
                                checked = thinkingEnabled,
                                onCheckedChange = { thinkingEnabled = it; syncAndSave() },
                                testTag = "toggle_thinking"
                            )

                            if (thinkingEnabled) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF4F4F5))
                                        .border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Text("Calibrated Thinking Budget", color = Color(0xFF18181B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Controls internal reasoning depth matching the official Google AI Studio tiers.",
                                        color = Color(0xFF71717A),
                                        fontSize = 11.5.sp,
                                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                    )

                                    // Official AI Studio Thinking Levels: Low, Medium, High
                                    val levels = listOf(
                                        Triple("Low", "Low", "Minimizes latency and token cost • best for straightforward tasks"),
                                        Triple("Medium", "Medium", "Balanced reasoning • optimal deduction for general multi-step tasks"),
                                        Triple("High", "High", "Maximizes reasoning depth • thorough deduction on complex problems")
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        levels.forEach { (key, title, desc) ->
                                            val isLevelSelected = thinkingLevel.equals(key, ignoreCase = true)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isLevelSelected) Color(0xFF18181B) else Color.White)
                                                    .border(1.dp, if (isLevelSelected) Color(0xFF18181B) else Color(0xFFE4E4E7), RoundedCornerShape(8.dp))
                                                    .clickable { thinkingLevel = key; syncAndSave() }
                                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(title, color = if (isLevelSelected) Color.White else Color(0xFF18181B), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                                    Text(desc, color = if (isLevelSelected) Color(0xFFD4D4D8) else Color(0xFF71717A), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        SettingsCategory.LANGUAGE_INSPECTOR -> {
                            MonochromeToggle(
                                title = "Contextual Smoothing",
                                description = "Smooths speech transcription boundaries and punctuation while preserving original words and errors intact.",
                                checked = contextualSmoothing,
                                onCheckedChange = { contextualSmoothing = it; syncAndSave() },
                                testTag = "toggle_contextual_smoothing"
                            )

                            MonochromeToggle(
                                title = "Grammar & Word Choice Inspector",
                                description = "Inspects inputs for linguistic improvements with interactive tap-to-fix popups.",
                                checked = grammarInspector,
                                onCheckedChange = { grammarInspector = it; syncAndSave() },
                                testTag = "toggle_grammar_inspector"
                            )
                        }

                        SettingsCategory.AI_PERSONA_VOICE -> {
                            // Top Bar: "+ Create Person"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("AI Persona & Voice", color = Color(0xFF18181B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Choosing a persona dictates both their acoustic voice and conversational worldview.", color = Color(0xFF71717A), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { showCreatePersonaDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("create_person_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Create", color = Color.White, fontSize = 12.5.sp)
                                }
                            }

                            // Filter Chips Row
                            val scrollRow = rememberScrollState()
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollRow),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "Male", "Female").forEach { g ->
                                    val isSel = personaGenderFilter == g
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color(0xFFF4F4F5)).clickable { personaGenderFilter = g }.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(g, color = if (isSel) Color.White else Color(0xFF3F3F46), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0xFFE4E4E7)))
                                listOf("All", "British", "American", "Australian", "European").forEach { r ->
                                    val isSel = personaRegionFilter == r
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color(0xFFF4F4F5)).clickable { personaRegionFilter = r }.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(r, color = if (isSel) Color.White else Color(0xFF3F3F46), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Custom Created Personas First
                            if (customPersonas.isNotEmpty()) {
                                Text("MY CREATED CHARACTERS", color = Color(0xFF71717A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                customPersonas.forEach { custom ->
                                    val isSelected = selectedVoicePersona == custom.id
                                    val isPreviewingThis = currentlySpeakingPersonaId == custom.id
                                    val isLoadingThis = isLoadingPersonaId == custom.id

                                    PersonaCardItem(
                                        name = custom.name,
                                        title = "${custom.accentRegion} (Custom)",
                                        style = custom.baseVoiceName,
                                        explanation = "Custom persona created in Studio.",
                                        sampleText = "Hello, I am ${custom.name}. Let's get straight to work.",
                                        isSelected = isSelected,
                                        isPreviewing = isPreviewingThis,
                                        isLoading = isLoadingThis,
                                        onSelect = { selectedVoicePersona = custom.id; syncAndSave() },
                                        onPreviewToggle = {
                                            if (isPreviewingThis) onStopPreviewVoicePersona?.invoke()
                                            else onPreviewVoicePersona?.invoke(custom.id)
                                        },
                                        onDelete = { onDeleteCustomPersona(custom.id) }
                                    )
                                }
                            }

                            Text("CURATED REAL CHARACTERS", color = Color(0xFF71717A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                            // Curated Personas Filtered
                            val filteredCurated = VoicePersonas.CURATED_PEOPLE.filter { p ->
                                (personaGenderFilter == "All" || p.gender == personaGenderFilter) &&
                                (personaRegionFilter == "All" || p.region == personaRegionFilter)
                            }

                            filteredCurated.forEach { p ->
                                val isSelected = selectedVoicePersona == p.id
                                val isPreviewingThis = currentlySpeakingPersonaId == p.id
                                val isLoadingThis = isLoadingPersonaId == p.id

                                PersonaCardItem(
                                    name = p.name,
                                    title = p.title,
                                    style = p.style,
                                    explanation = p.explanation,
                                    sampleText = p.sampleText,
                                    isSelected = isSelected,
                                    isPreviewing = isPreviewingThis,
                                    isLoading = isLoadingThis,
                                    onSelect = { selectedVoicePersona = p.id; syncAndSave() },
                                    onPreviewToggle = {
                                        if (isPreviewingThis) onStopPreviewVoicePersona?.invoke()
                                        else onPreviewVoicePersona?.invoke(p.id)
                                    }
                                )
                            }
                        }

                        SettingsCategory.VOICE_INPUT -> {
                            // PROMINENT DEDICATED MODEL CARD: Proves & explicitly documents Gemini 3.5 Flash-Lite
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF090A0C))
                                    .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp))
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Voice & Audio Model",
                                                color = Color.White,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Gemini 3.5 Flash-Lite",
                                                color = Color(0xFFA1A1AA),
                                                fontSize = 12.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF27272A))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Non-limited dynamic alias: gemini-flash-lite-latest",
                                        color = Color(0xFFF4F4F5),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "Powered by Google's non-limited dynamic model alias. This ensures continuous zero-latency audio comprehension and avoids hardcoded preview date deprecation.",
                                        color = Color(0xFF71717A),
                                        fontSize = 11.5.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).padding(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Microphone Sensitivity", color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text(micSensitivity, color = Color(0xFF18181B), fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Threshold for separating speech from background silence.", color = Color(0xFF71717A), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("High", "Normal", "Low").forEach { level ->
                                        val isSelected = micSensitivity == level
                                        Box(
                                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFF18181B) else Color.White).border(1.dp, if (isSelected) Color(0xFF18181B) else Color(0xFFD4D4D8), RoundedCornerShape(8.dp)).clickable { micSensitivity = level; syncAndSave() }.padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(level, color = if (isSelected) Color.White else Color(0xFF3F3F46), fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).padding(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Silence Pause Duration", color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("${String.format(java.util.Locale.US, "%.1fs", silencePauseDuration)}", color = Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = silencePauseDuration,
                                    onValueChange = { silencePauseDuration = it; syncAndSave() },
                                    valueRange = 0.8f..4.0f,
                                    steps = 15,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF18181B), activeTrackColor = Color(0xFF18181B), inactiveTrackColor = Color(0xFFD4D4D8))
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).padding(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Minimum Voice Hold Filter", color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    Text("${minVoiceHoldDurationSeconds.toInt()}s", color = Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("Touches shorter than this duration are rejected as unintended touches.", color = Color(0xFF71717A), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(1f, 2f, 3f, 4f, 5f).forEach { sec ->
                                        val isSel = (minVoiceHoldDurationSeconds.toInt() == sec.toInt())
                                        Box(
                                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color.White).border(1.dp, if (isSel) Color(0xFF18181B) else Color(0xFFD4D4D8), RoundedCornerShape(8.dp)).clickable { minVoiceHoldDurationSeconds = sec; syncAndSave() }.padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${sec.toInt()}s", color = if (isSel) Color.White else Color(0xFF3F3F46), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            Text("HAPTIC WAVE PATTERNS (10 CADENCES)", color = Color(0xFF71717A), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                            VibrationPatterns.ALL.forEach { pattern ->
                                val isSelected = selectedVibrationPattern == pattern.id
                                val isPreviewingThis = previewingPatternId == pattern.id
                                Box(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (isSelected) Color(0xFF18181B) else Color(0xFFF4F4F5)).border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Color(0xFF18181B) else Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).clickable { selectedVibrationPattern = pattern.id; syncAndSave() }.padding(14.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(pattern.name, color = if (isSelected) Color.White else Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(pattern.persona, color = if (isSelected) Color(0xFFD4D4D8) else Color(0xFF71717A), fontSize = 11.5.sp)
                                        }
                                        Box(
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color(0xFF27272A) else Color.White).border(1.dp, Color(0xFFD4D4D8), RoundedCornerShape(8.dp)).clickable {
                                                if (isPreviewingThis) {
                                                    onStopPreviewVibration?.invoke()
                                                    previewingPatternId = null
                                                } else {
                                                    previewingPatternId = pattern.id
                                                    onPreviewVibration?.invoke(pattern.id)
                                                    coroutineScope.launch {
                                                        delay(2600L)
                                                        if (previewingPatternId == pattern.id) previewingPatternId = null
                                                    }
                                                }
                                            }.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(if (isPreviewingThis) "Stop" else "Try", color = if (isSelected) Color.White else Color(0xFF18181B), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        SettingsCategory.LONG_TERM_MEMORY -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF18181B)).clickable { onOpenMemoryVault() }.padding(16.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Open Memory Vault", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("View and search $memoryCount long-term notes", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                                    }
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }

                            MonochromeToggle(
                                title = "Enable Long-Term Memory",
                                description = "Injects retrieved high-relevance memories into context window for deep personalization.",
                                checked = longTermMemoryEnabled,
                                onCheckedChange = { longTermMemoryEnabled = it; syncAndSave() },
                                testTag = "toggle_long_term_memory"
                            )
                        }

                        SettingsCategory.API_KEYS -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF4F4F5)).padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (apiKeyTab == 0) Color(0xFF18181B) else Color.Transparent).clickable { apiKeyTab = 0 }.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("AI Studio APIs (${apiKeys.size})", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (apiKeyTab == 0) Color.White else Color(0xFF71717A))
                                }
                                Box(
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (apiKeyTab == 1) Color(0xFF18181B) else Color.Transparent).clickable { apiKeyTab = 1 }.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Tavily APIs (${tavilyApiKeys.size})", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = if (apiKeyTab == 1) Color.White else Color(0xFF71717A))
                                }
                            }

                            if (apiKeyTab == 0) {
                                apiKeys.forEachIndexed { idx, item ->
                                    MonochromeKeyCard(item = item, index = idx + 1, onDelete = { onDeleteKey(item.id) })
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newKeyInput,
                                        onValueChange = { newKeyInput = it },
                                        placeholder = { Text("Paste Gemini Key (AIza...)", fontSize = 13.sp, color = Color(0xFF71717A)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { if (newKeyInput.isNotBlank()) { onAddKey(newKeyInput.trim()); newKeyInput = "" } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("Add", color = Color.White) }
                                }
                            } else {
                                tavilyApiKeys.forEachIndexed { idx, item ->
                                    MonochromeKeyCard(item = item, index = idx + 1, onDelete = { onDeleteTavilyKey(item.id) })
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newTavilyKeyInput,
                                        onValueChange = { newTavilyKeyInput = it },
                                        placeholder = { Text("Paste Tavily Key (tvly-...)", fontSize = 13.sp, color = Color(0xFF71717A)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { if (newTavilyKeyInput.isNotBlank()) { onAddTavilyKey(newTavilyKeyInput.trim()); newTavilyKeyInput = "" } },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) { Text("Add", color = Color.White) }
                                }
                            }
                        }

                        SettingsCategory.WEB_SEARCH -> {
                            Text("Web Search Thoroughness", color = Color(0xFF18181B), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            val levels = listOf(
                                Triple("Fast", "Quick Glance", "1-2 sources • lowest latency"),
                                Triple("Balanced", "Balanced", "3-5 sources • optimal depth"),
                                Triple("Comprehensive", "Comprehensive", "6-8 sources • deep web research")
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                levels.forEach { (key, label, desc) ->
                                    val isSelected = tavilyThoroughness == key
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isSelected) Color(0xFF18181B) else Color.White).border(1.dp, if (isSelected) Color(0xFF18181B) else Color(0xFFE4E4E7), RoundedCornerShape(10.dp)).clickable { tavilyThoroughness = key; syncAndSave() }.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(label, color = if (isSelected) Color.White else Color(0xFF18181B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Text(desc, color = if (isSelected) Color(0xFFA1A1AA) else Color(0xFF71717A), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Persona Studio Modal
    if (showCreatePersonaDialog) {
        PersonaStudioDialog(
            onDismiss = { showCreatePersonaDialog = false },
            onSave = { created ->
                onSaveCustomPersona(created)
                showCreatePersonaDialog = false
            }
        )
    }
}

@Composable
private fun PersonaCardItem(
    name: String,
    title: String,
    style: String,
    explanation: String,
    sampleText: String,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isLoading: Boolean,
    onSelect: () -> Unit,
    onPreviewToggle: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) Color(0xFF18181B) else Color(0xFFF4F4F5))
            .border(1.dp, if (isSelected) Color(0xFF18181B) else Color(0xFFE4E4E7), RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .border(1.5.dp, if (isSelected) Color.White else Color(0xFF71717A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF18181B), modifier = Modifier.size(13.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$name • $title", color = if (isSelected) Color.White else Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                Text(style, color = if (isSelected) Color(0xFFD4D4D8) else Color(0xFF71717A), fontSize = 11.5.sp)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPreviewing) (if (isSelected) Color.White else Color(0xFF18181B)) else (if (isSelected) Color(0xFF27272A) else Color.White))
                    .border(1.dp, Color(0xFFD4D4D8), RoundedCornerShape(8.dp))
                    .clickable(onClick = onPreviewToggle)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = if (isSelected) Color.White else Color.Black, strokeWidth = 1.5.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, tint = if (isPreviewing) (if (isSelected) Color.Black else Color.White) else (if (isSelected) Color.White else Color.Black), modifier = Modifier.size(13.dp))
                        Text(if (isPreviewing) "Stop" else "Try", color = if (isPreviewing) (if (isSelected) Color.Black else Color.White) else (if (isSelected) Color.White else Color.Black), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(explanation, color = if (isSelected) Color(0xFFD4D4D8) else Color(0xFF52525B), fontSize = 12.sp, modifier = Modifier.padding(start = 30.dp))
        Text("\"$sampleText\"", color = if (isSelected) Color(0xFFA1A1AA) else Color(0xFF71717A), fontSize = 11.5.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(start = 30.dp, top = 2.dp))
    }
}

@Composable
fun PersonaStudioDialog(
    onDismiss: () -> Unit,
    onSave: (CustomPersona) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var baseVoiceName by remember { mutableStateOf("Charon") }
    var accentRegion by remember { mutableStateOf("British (Northern / Yorkshire)") }

    var slangLevel by remember { mutableFloatStateOf(0.4f) }
    var warmthLevel by remember { mutableFloatStateOf(0.5f) }
    var outlookLevel by remember { mutableFloatStateOf(0.6f) }
    var directnessLevel by remember { mutableFloatStateOf(0.7f) }
    var verbosityLevel by remember { mutableFloatStateOf(0.3f) }

    val scroll = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Persona Studio • Create Person", fontWeight = FontWeight.Bold, color = Color(0xFF18181B)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(480.dp).verticalScroll(scroll)) {
                Text("Stage 1: Identity & Accent", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF18181B))
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Person's Name (e.g. David)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Male", "Female", "Neutral").forEach { g ->
                        val isSel = gender == g
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color(0xFFF4F4F5)).clickable { gender = g }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(g, color = if (isSel) Color.White else Color(0xFF3F3F46), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Region & Accent:", fontSize = 12.sp, color = Color(0xFF71717A))
                val accentScroll = rememberScrollState()
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(accentScroll), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    VoicePersonas.REGIONAL_ACCENTS.forEach { acc ->
                        val isSel = accentRegion == acc
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color(0xFFF4F4F5)).clickable { accentRegion = acc }.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(acc, color = if (isSel) Color.White else Color(0xFF3F3F46), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Stage 2: Acoustic Baseline Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF18181B))
                val matchingVoices = VoicePersonas.BASELINE_VOICES.filter { it.gender == gender }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    matchingVoices.forEach { v ->
                        val isSel = baseVoiceName == v.name
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isSel) Color(0xFF18181B) else Color(0xFFF4F4F5)).clickable { baseVoiceName = v.name }.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(v.name, color = if (isSel) Color.White else Color(0xFF18181B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(v.acousticTexture, color = if (isSel) Color(0xFFD4D4D8) else Color(0xFF71717A), fontSize = 10.5.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Stage 3: Personality Spectrum Sliders", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF18181B))

                SpectrumSliderItem(title = "Register", leftLabel = "Street Slang", rightLabel = "Corporate", value = slangLevel, onValueChange = { slangLevel = it })
                SpectrumSliderItem(title = "Social Warmth", leftLabel = "Compassionate", rightLabel = "Cold / Cynical", value = warmthLevel, onValueChange = { warmthLevel = it })
                SpectrumSliderItem(title = "Worldview", leftLabel = "Optimist", rightLabel = "Pessimist", value = outlookLevel, onValueChange = { outlookLevel = it })
                SpectrumSliderItem(title = "Directness", leftLabel = "Diplomatic", rightLabel = "Brutally Blunt", value = directnessLevel, onValueChange = { directnessLevel = it })
                SpectrumSliderItem(title = "Verbosity", leftLabel = "Terse (Few Words)", rightLabel = "Expansive", value = verbosityLevel, onValueChange = { verbosityLevel = it })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            CustomPersona(
                                name = name.trim(),
                                gender = gender,
                                baseVoiceName = baseVoiceName,
                                accentRegion = accentRegion,
                                slangLevel = slangLevel,
                                warmthLevel = warmthLevel,
                                outlookLevel = outlookLevel,
                                directnessLevel = directnessLevel,
                                verbosityLevel = verbosityLevel
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18181B))
            ) { Text("Save Person", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF71717A)) }
        },
        containerColor = Color.White
    )
}

@Composable
private fun SpectrumSliderItem(
    title: String,
    leftLabel: String,
    rightLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(leftLabel, fontSize = 10.5.sp, color = Color(0xFF71717A))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF18181B))
            Text(rightLabel, fontSize = 10.5.sp, color = Color(0xFF71717A))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(thumbColor = Color(0xFF18181B), activeTrackColor = Color(0xFF18181B), inactiveTrackColor = Color(0xFFD4D4D8))
        )
    }
}

@Composable
private fun CategoryHubCard(category: SettingsCategory, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(Color(0xFFE4E4E7)), contentAlignment = Alignment.Center) {
                Icon(imageVector = category.icon, contentDescription = null, tint = Color(0xFF18181B), modifier = Modifier.size(19.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.title, color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                Text(category.description, color = Color(0xFF71717A), fontSize = 12.sp)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color(0xFF71717A), modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun MonochromeToggle(title: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, testTag: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF4F4F5)).border(1.dp, Color(0xFFE4E4E7), RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color(0xFF18181B), fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(description, color = Color(0xFF71717A), fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF18181B), uncheckedThumbColor = Color(0xFF71717A), uncheckedTrackColor = Color(0xFFE4E4E7), uncheckedBorderColor = Color(0xFFD4D4D8)),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun MonochromeKeyCard(item: ApiKeyItem, index: Int, onDelete: () -> Unit) {
    val isFailed = item.status == KeyStatus.FAILED
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (isFailed) Color(0xFFFEF2F2) else Color(0xFFF4F4F5)).border(1.dp, if (isFailed) Color(0xFFEF4444) else Color(0xFFE4E4E7), RoundedCornerShape(10.dp)).padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (isFailed) Color(0xFFEF4444) else Color(0xFF10B981)))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.label, color = Color(0xFF18181B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(item.maskedKey, color = Color(0xFF71717A), fontSize = 11.5.sp)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF71717A), modifier = Modifier.size(16.dp))
            }
        }
    }
}
