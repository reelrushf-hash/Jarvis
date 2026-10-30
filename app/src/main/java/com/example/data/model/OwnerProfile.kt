package com.example.data.model

data class OwnerProfile(
    val name: String = "Boss",
    val securityPin: String = "1122",
    val isVerified: Boolean = true,
    val voiceEnabled: Boolean = true,
    val preferredLanguage: Language = Language.ENGLISH,
    val memoryBudgetMb: Int = 500,
    val onlineAiEnabled: Boolean = true,
    val lowRamMode: Boolean = true,
    val speechPitch: Float = 1.0f,
    val speechSpeed: Float = 1.0f,
    val floatingButtonEnabled: Boolean = false
)

enum class BrainType(val label: String) {
    OFFLINE("Offline Brain"),
    ONLINE("Gemini AI")
}
