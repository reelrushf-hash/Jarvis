package com.example.brain

import com.example.data.model.Language

sealed class BrainDecision {
    data class ExecuteAction(
        val actionType: ActionType,
        val target: String = "",
        val extraData: String = "",
        val spokenResponse: String,
        val brainUsed: String = "Offline Brain"
    ) : BrainDecision()

    data class SpokenAnswer(
        val spokenResponse: String,
        val brainUsed: String = "Offline Brain"
    ) : BrainDecision()

    data class RequireConfirmation(
        val actionType: ActionType,
        val target: String,
        val prompt: String,
        val extraData: String = ""
    ) : BrainDecision()

    data class EmergencyStop(
        val spokenResponse: String = "Emergency stop engaged, Boss. All tasks suspended."
    ) : BrainDecision()
}

enum class ActionType {
    CALL,
    OPEN_APP,
    SEARCH_YOUTUBE,
    SEND_MESSAGE,
    SAVE_FACT,
    RECALL_FACT,
    ADD_TASK,
    DEVICE_STATUS
}
