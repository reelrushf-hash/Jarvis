package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.brain.ActionType
import com.example.brain.BrainDecision
import com.example.brain.BrainRouter
import com.example.brain.OfflineBrain
import com.example.brain.OnlineBrain
import com.example.data.db.JarvisDatabase
import com.example.data.db.entity.ContactMemoryEntity
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.FactEntity
import com.example.data.db.entity.KnowledgeEntity
import com.example.data.db.entity.TaskEntity
import com.example.data.model.Language
import com.example.data.model.OwnerProfile
import com.example.data.repository.JarvisMemoryRepository
import com.example.data.repository.MemoryStats
import com.example.system.CallManager
import com.example.system.DeviceDiagnostics
import com.example.system.MessagingManager
import com.example.system.SystemActionManager
import com.example.system.VoiceEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PendingActionConfirmation(
    val actionType: ActionType,
    val target: String,
    val prompt: String,
    val extraData: String = ""
)

data class JarvisUiState(
    val ownerProfile: OwnerProfile = OwnerProfile(),
    val currentLanguage: Language = Language.ENGLISH,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val lastResponse: String = "JARVIS AI Online. Awaiting your command, Boss.",
    val lastBrainUsed: String = "Offline Brain",
    val partialTranscript: String = "",
    val memoryStats: MemoryStats = MemoryStats(0L, 0.0, 500, 0, 0, 0, 0, 0),
    val diagnostics: DeviceDiagnostics = DeviceDiagnostics(2048, 1024, false, 85),
    val pendingConfirmation: PendingActionConfirmation? = null,
    val showPinVerification: Boolean = false,
    val pendingSensitiveAction: (() -> Unit)? = null,
    val statusMessage: String? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val database = JarvisDatabase.getDatabase(application)
    val repository = JarvisMemoryRepository(application, database.jarvisDao())

    private val systemActionManager = SystemActionManager(application)
    private val callManager = CallManager(application, repository)
    private val messagingManager = MessagingManager(application)

    private val offlineBrain = OfflineBrain(repository)
    private val onlineBrain = OnlineBrain()
    private val brainRouter = BrainRouter(application, offlineBrain, onlineBrain)

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val facts: StateFlow<List<FactEntity>> = repository.facts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactMemoryEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val knowledge: StateFlow<List<KnowledgeEntity>> = repository.knowledge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private val voiceEngine = VoiceEngine(application) { finalTranscript ->
        if (finalTranscript.isNotBlank()) {
            executeCommand(finalTranscript)
        }
    }

    init {
        loadProfileAndDiagnostics()

        // Track voice engine state
        viewModelScope.launch {
            voiceEngine.isListening.collect { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            }
        }
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }
        viewModelScope.launch {
            voiceEngine.livePartialTranscript.collect { partial ->
                _uiState.value = _uiState.value.copy(partialTranscript = partial)
            }
        }
    }

    fun loadProfileAndDiagnostics() {
        viewModelScope.launch {
            val profile = repository.getOwnerProfile()
            val stats = repository.getMemoryUsageStats(profile.memoryBudgetMb)
            val diag = systemActionManager.getDeviceDiagnostics()

            voiceEngine.setLanguage(profile.preferredLanguage)

            _uiState.value = _uiState.value.copy(
                ownerProfile = profile,
                currentLanguage = profile.preferredLanguage,
                memoryStats = stats,
                diagnostics = diag
            )
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            val updated = _uiState.value.ownerProfile.copy(preferredLanguage = language)
            repository.saveOwnerProfile(updated)
            voiceEngine.setLanguage(language)
            _uiState.value = _uiState.value.copy(
                ownerProfile = updated,
                currentLanguage = language
            )
            val greeting = language.greetingBoss
            speakOut(greeting)
        }
    }

    fun startListening() {
        voiceEngine.setLanguage(_uiState.value.currentLanguage)
        voiceEngine.startListening()
    }

    fun stopListening() {
        voiceEngine.stopListening()
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun emergencyStop() {
        voiceEngine.stopSpeaking()
        voiceEngine.stopListening()
        _uiState.value = _uiState.value.copy(
            isListening = false,
            isSpeaking = false,
            isProcessing = false,
            pendingConfirmation = null,
            lastResponse = "Emergency stop engaged, Boss. All tasks suspended.",
            statusMessage = "EMERGENCY STOP"
        )
        viewModelScope.launch {
            repository.logAudit("EMERGENCY_STOP", "HIGH", true, "Boss triggered global emergency abort")
        }
    }

    fun executeCommand(rawCommand: String) {
        if (rawCommand.isBlank()) return
        val currentLang = _uiState.value.currentLanguage
        val profile = _uiState.value.ownerProfile

        _uiState.value = _uiState.value.copy(isProcessing = true, partialTranscript = "")

        viewModelScope.launch {
            // Log Boss input
            repository.logConversation("BOSS", rawCommand, currentLang, "Voice/Text Input")

            val decision = brainRouter.route(rawCommand, currentLang, profile)
            _uiState.value = _uiState.value.copy(isProcessing = false)

            when (decision) {
                is BrainDecision.EmergencyStop -> {
                    emergencyStop()
                }
                is BrainDecision.SpokenAnswer -> {
                    _uiState.value = _uiState.value.copy(
                        lastResponse = decision.spokenResponse,
                        lastBrainUsed = decision.brainUsed
                    )
                    repository.logConversation("JARVIS", decision.spokenResponse, currentLang, decision.brainUsed)
                    speakOut(decision.spokenResponse)
                    refreshMemoryStats()
                }
                is BrainDecision.RequireConfirmation -> {
                    _uiState.value = _uiState.value.copy(
                        pendingConfirmation = PendingActionConfirmation(
                            actionType = decision.actionType,
                            target = decision.target,
                            prompt = decision.prompt,
                            extraData = decision.extraData
                        ),
                        lastResponse = decision.prompt
                    )
                    speakOut(decision.prompt)
                }
                is BrainDecision.ExecuteAction -> {
                    executePermittedAction(decision.actionType, decision.target, decision.extraData, decision.spokenResponse)
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        _uiState.value = _uiState.value.copy(pendingConfirmation = null)
        executePermittedAction(pending.actionType, pending.target, pending.extraData, "")
    }

    fun dismissPendingAction() {
        _uiState.value = _uiState.value.copy(pendingConfirmation = null)
        val cancelMsg = "Action cancelled as requested, Boss."
        _uiState.value = _uiState.value.copy(lastResponse = cancelMsg)
        speakOut(cancelMsg)
    }

    private fun executePermittedAction(type: ActionType, target: String, extra: String, initialSpoken: String) {
        viewModelScope.launch {
            when (type) {
                ActionType.CALL -> {
                    val contact = callManager.resolveContact(target)
                    val phone = contact?.phoneNumber ?: target
                    val callResult = callManager.initiateCall(phone, isDirectCall = true)
                    _uiState.value = _uiState.value.copy(
                        lastResponse = callResult.spokenMessage,
                        lastBrainUsed = "Call Manager"
                    )
                    repository.logConversation("JARVIS", callResult.spokenMessage, _uiState.value.currentLanguage, "Call Manager")
                    repository.logAudit("PHONE_CALL", "MEDIUM", true, "Placed call to $phone")
                    speakOut(callResult.spokenMessage)
                }
                ActionType.OPEN_APP -> {
                    val appResult = systemActionManager.launchApp(target)
                    _uiState.value = _uiState.value.copy(
                        lastResponse = appResult.spokenFeedback,
                        lastBrainUsed = "System Action"
                    )
                    repository.logConversation("JARVIS", appResult.spokenFeedback, _uiState.value.currentLanguage, "System Action")
                    speakOut(appResult.spokenFeedback)
                }
                ActionType.SEARCH_YOUTUBE -> {
                    val ytResult = systemActionManager.openYouTubeSearch(target)
                    _uiState.value = _uiState.value.copy(
                        lastResponse = ytResult.spokenFeedback,
                        lastBrainUsed = "YouTube Action"
                    )
                    repository.logConversation("JARVIS", ytResult.spokenFeedback, _uiState.value.currentLanguage, "YouTube Action")
                    speakOut(ytResult.spokenFeedback)
                }
                ActionType.SAVE_FACT -> {
                    repository.saveFact("Boss Note", target, "User Fact")
                    val feedback = initialSpoken.ifEmpty { "Saved to memory vault, Boss." }
                    _uiState.value = _uiState.value.copy(
                        lastResponse = feedback,
                        lastBrainUsed = "Memory Vault"
                    )
                    repository.logConversation("JARVIS", feedback, _uiState.value.currentLanguage, "Memory Vault")
                    speakOut(feedback)
                    refreshMemoryStats()
                }
                ActionType.ADD_TASK -> {
                    repository.addTask(target)
                    val feedback = initialSpoken.ifEmpty { "Task added, Boss." }
                    _uiState.value = _uiState.value.copy(
                        lastResponse = feedback,
                        lastBrainUsed = "Task Memory"
                    )
                    repository.logConversation("JARVIS", feedback, _uiState.value.currentLanguage, "Task Memory")
                    speakOut(feedback)
                    refreshMemoryStats()
                }
                ActionType.DEVICE_STATUS -> {
                    val diag = systemActionManager.getDeviceDiagnostics()
                    val feedback = "Telemetry: RAM Available ${diag.availableRamMb} MB of ${diag.totalRamMb} MB. Battery: ${diag.batteryPercentage}%."
                    _uiState.value = _uiState.value.copy(
                        lastResponse = feedback,
                        diagnostics = diag,
                        lastBrainUsed = "Diagnostics"
                    )
                    repository.logConversation("JARVIS", feedback, _uiState.value.currentLanguage, "Diagnostics")
                    speakOut(feedback)
                }
                ActionType.SEND_MESSAGE -> {
                    messagingManager.sendWhatsApp(target, extra)
                }
                ActionType.RECALL_FACT -> {
                    // Handled directly in OfflineBrain
                }
            }
        }
    }

    private fun speakOut(text: String) {
        val speed = _uiState.value.ownerProfile.speechSpeed
        val pitch = _uiState.value.ownerProfile.speechPitch
        voiceEngine.speak(text, speed, pitch)
    }

    fun replayLastResponse() {
        val text = _uiState.value.lastResponse
        if (text.isNotBlank()) {
            speakOut(text)
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
    }

    fun refreshMemoryStats() {
        viewModelScope.launch {
            val stats = repository.getMemoryUsageStats(_uiState.value.ownerProfile.memoryBudgetMb)
            _uiState.value = _uiState.value.copy(memoryStats = stats)
        }
    }

    // Task actions
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
            refreshMemoryStats()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
            refreshMemoryStats()
        }
    }

    fun addTaskManual(title: String, notes: String = "") {
        viewModelScope.launch {
            repository.addTask(title, notes)
            refreshMemoryStats()
        }
    }

    // Fact actions
    fun addFactManual(subject: String, content: String, category: String) {
        viewModelScope.launch {
            repository.saveFact(subject, content, category)
            refreshMemoryStats()
        }
    }

    fun deleteFact(id: Long) {
        viewModelScope.launch {
            repository.deleteFact(id)
            refreshMemoryStats()
        }
    }

    // Contact actions
    fun addContactManual(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            repository.saveContact(name, phone, relationship)
            refreshMemoryStats()
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContact(id)
            refreshMemoryStats()
        }
    }

    // Knowledge actions
    fun addKnowledgeManual(title: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.saveKnowledge(title, content, tags)
            refreshMemoryStats()
        }
    }

    fun deleteKnowledge(id: Long) {
        viewModelScope.launch {
            repository.deleteKnowledge(id)
            refreshMemoryStats()
        }
    }

    // Settings & PIN verification
    fun updateProfile(profile: OwnerProfile) {
        viewModelScope.launch {
            repository.saveOwnerProfile(profile)
            _uiState.value = _uiState.value.copy(ownerProfile = profile)
            voiceEngine.setLanguage(profile.preferredLanguage)
        }
    }

    fun requestSensitiveActionWithPin(action: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            showPinVerification = true,
            pendingSensitiveAction = action
        )
    }

    fun verifyPinAndExecute(enteredPin: String): Boolean {
        val correctPin = _uiState.value.ownerProfile.securityPin
        if (enteredPin == correctPin) {
            val action = _uiState.value.pendingSensitiveAction
            _uiState.value = _uiState.value.copy(
                showPinVerification = false,
                pendingSensitiveAction = null
            )
            action?.invoke()
            return true
        }
        return false
    }

    fun dismissPinDialog() {
        _uiState.value = _uiState.value.copy(
            showPinVerification = false,
            pendingSensitiveAction = null
        )
    }

    fun wipeAllMemoriesConfirmed() {
        viewModelScope.launch {
            repository.wipeAllMemories()
            refreshMemoryStats()
            val msg = "All memories wiped as authorized by Boss."
            _uiState.value = _uiState.value.copy(lastResponse = msg)
            speakOut(msg)
        }
    }

    fun pruneOldLogs() {
        viewModelScope.launch {
            repository.pruneOldLogs()
            refreshMemoryStats()
            _uiState.value = _uiState.value.copy(lastResponse = "Old logs pruned to maintain 500MB budget, Boss.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
