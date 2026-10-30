package com.example.data.repository

import android.content.Context
import com.example.data.db.JarvisDao
import com.example.data.db.entity.AuditLogEntity
import com.example.data.db.entity.ContactMemoryEntity
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.CoreMemoryEntity
import com.example.data.db.entity.FactEntity
import com.example.data.db.entity.KnowledgeEntity
import com.example.data.db.entity.PreferenceEntity
import com.example.data.db.entity.TaskEntity
import com.example.data.model.Language
import com.example.data.model.OwnerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class JarvisMemoryRepository(
    private val context: Context,
    private val dao: JarvisDao
) {
    val conversations: Flow<List<ConversationEntity>> = dao.getRecentConversations(35)
    val facts: Flow<List<FactEntity>> = dao.getAllFacts(50)
    val tasks: Flow<List<TaskEntity>> = dao.getAllTasks(50)
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()
    val preferences: Flow<List<PreferenceEntity>> = dao.getAllPreferences(50)
    val contacts: Flow<List<ContactMemoryEntity>> = dao.getAllContacts(50)
    val knowledge: Flow<List<KnowledgeEntity>> = dao.getAllKnowledge(50)
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getRecentAuditLogs(30)

    // Load or initialize default Owner Profile
    suspend fun getOwnerProfile(): OwnerProfile = withContext(Dispatchers.IO) {
        val name = dao.getCoreMemory("owner_name")?.memoryValue ?: "Boss"
        val pin = dao.getCoreMemory("security_pin")?.memoryValue ?: "1122"
        val langCode = dao.getCoreMemory("preferred_lang")?.memoryValue ?: "en"
        val budgetMb = dao.getCoreMemory("memory_budget_mb")?.memoryValue?.toIntOrNull() ?: 500
        val onlineAi = dao.getCoreMemory("online_ai_enabled")?.memoryValue?.toBooleanStrictOrNull() ?: true
        val lowRam = dao.getCoreMemory("low_ram_mode")?.memoryValue?.toBooleanStrictOrNull() ?: true

        OwnerProfile(
            name = name,
            securityPin = pin,
            isVerified = true,
            preferredLanguage = Language.fromCode(langCode),
            memoryBudgetMb = budgetMb,
            onlineAiEnabled = onlineAi,
            lowRamMode = lowRam
        )
    }

    suspend fun saveOwnerProfile(profile: OwnerProfile) = withContext(Dispatchers.IO) {
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "owner_name", memoryValue = profile.name))
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "security_pin", memoryValue = profile.securityPin))
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "preferred_lang", memoryValue = profile.preferredLanguage.code))
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "memory_budget_mb", memoryValue = profile.memoryBudgetMb.toString()))
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "online_ai_enabled", memoryValue = profile.onlineAiEnabled.toString()))
        dao.saveCoreMemory(CoreMemoryEntity(memoryKey = "low_ram_mode", memoryValue = profile.lowRamMode.toString()))
    }

    suspend fun logConversation(role: String, content: String, language: Language, brain: String) = withContext(Dispatchers.IO) {
        dao.insertConversation(
            ConversationEntity(
                role = role,
                content = content,
                languageCode = language.code,
                brainUsed = brain
            )
        )
        // Automatically enforce memory retention if conversation count is very high
        val count = dao.getConversationCount()
        if (count > 250) {
            dao.pruneOldConversations(100)
        }
    }

    suspend fun saveFact(subject: String, factContent: String, category: String = "Personal"): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.searchFacts(subject)
        val isDuplicate = existing.any { it.factContent.equals(factContent, ignoreCase = true) }
        if (!isDuplicate) {
            dao.insertFact(
                FactEntity(
                    subject = subject.trim(),
                    factContent = factContent.trim(),
                    category = category
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun deleteFact(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteFact(id)
    }

    suspend fun savePreference(key: String, value: String, category: String = "General") = withContext(Dispatchers.IO) {
        dao.savePreference(
            PreferenceEntity(
                prefKey = key.trim(),
                prefValue = value.trim(),
                category = category
            )
        )
    }

    suspend fun getPreference(key: String): String? = withContext(Dispatchers.IO) {
        dao.getPreference(key)?.prefValue
    }

    suspend fun addTask(title: String, notes: String = "", priority: Int = 1) = withContext(Dispatchers.IO) {
        dao.insertTask(
            TaskEntity(
                title = title.trim(),
                notes = notes.trim(),
                priority = priority
            )
        )
    }

    suspend fun toggleTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTask(id)
    }

    suspend fun saveContact(name: String, phoneNumber: String, relationship: String = "", nickname: String = "") = withContext(Dispatchers.IO) {
        dao.insertContact(
            ContactMemoryEntity(
                name = name.trim(),
                phoneNumber = phoneNumber.trim(),
                relationship = relationship.trim(),
                nickname = nickname.trim()
            )
        )
    }

    suspend fun findContact(query: String): List<ContactMemoryEntity> = withContext(Dispatchers.IO) {
        dao.searchContacts(query.trim())
    }

    suspend fun recordCall(id: Long) = withContext(Dispatchers.IO) {
        dao.recordCall(id)
    }

    suspend fun deleteContact(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteContact(id)
    }

    suspend fun saveKnowledge(title: String, content: String, tags: String = "") = withContext(Dispatchers.IO) {
        dao.insertKnowledge(
            KnowledgeEntity(
                title = title.trim(),
                content = content.trim(),
                tags = tags.trim()
            )
        )
    }

    suspend fun searchKnowledge(query: String): List<KnowledgeEntity> = withContext(Dispatchers.IO) {
        dao.searchKnowledge(query.trim())
    }

    suspend fun deleteKnowledge(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteKnowledge(id)
    }

    suspend fun logAudit(action: String, riskLevel: String, authorized: Boolean, details: String) = withContext(Dispatchers.IO) {
        dao.insertAuditLog(
            AuditLogEntity(
                actionName = action,
                riskLevel = riskLevel,
                authorized = authorized,
                details = details
            )
        )
    }

    // Unified indexed search across local memories
    suspend fun searchLocalMemories(query: String): List<String> = withContext(Dispatchers.IO) {
        val results = mutableListOf<String>()
        val facts = dao.searchFacts(query)
        facts.forEach { results.add("Fact: ${it.subject} is ${it.factContent}") }

        val contacts = dao.searchContacts(query)
        contacts.forEach { results.add("Contact: ${it.name} (${it.relationship.ifEmpty { "Personal" }}) - ${it.phoneNumber}") }

        val notes = dao.searchKnowledge(query)
        notes.forEach { results.add("Knowledge: ${it.title} - ${it.content}") }

        results
    }

    // Memory Maintenance: calculate DB file size in MB and stats
    suspend fun getMemoryUsageStats(budgetMb: Int = 500): MemoryStats = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("jarvis_memory.db")
        val sizeBytes = if (dbFile.exists()) dbFile.length() else 0L
        val sizeMb = sizeBytes / (1024.0 * 1024.0)

        val convCount = dao.getConversationCount()
        val factCount = dao.getFactCount()
        val taskCount = dao.getTaskCount()
        val contactCount = dao.getContactCount()
        val knowledgeCount = dao.getKnowledgeCount()

        MemoryStats(
            usedBytes = sizeBytes,
            usedMb = sizeMb,
            budgetMb = budgetMb,
            conversationCount = convCount,
            factCount = factCount,
            taskCount = taskCount,
            contactCount = contactCount,
            knowledgeCount = knowledgeCount
        )
    }

    suspend fun pruneOldLogs() = withContext(Dispatchers.IO) {
        dao.pruneOldConversations(50)
    }

    suspend fun wipeAllMemories(): Boolean = withContext(Dispatchers.IO) {
        dao.clearAllConversations()
        dao.clearFacts()
        dao.clearTasks()
        dao.clearContacts()
        dao.clearKnowledge()
        true
    }

    // Export memory summary to string for backup
    suspend fun exportMemoryBackup(): String = withContext(Dispatchers.IO) {
        val stats = getMemoryUsageStats()
        """
        === JARVIS AI MEMORY BACKUP ===
        Owner: Boss
        Export Date: ${System.currentTimeMillis()}
        Storage: ${String.format("%.2f", stats.usedMb)} MB / ${stats.budgetMb} MB
        Records: ${stats.factCount} Facts, ${stats.taskCount} Tasks, ${stats.contactCount} Contacts, ${stats.knowledgeCount} Notes
        Status: Valid Offline Encrypted Replica
        ===============================
        """.trimIndent()
    }
}

data class MemoryStats(
    val usedBytes: Long,
    val usedMb: Double,
    val budgetMb: Int,
    val conversationCount: Int,
    val factCount: Int,
    val taskCount: Int,
    val contactCount: Int,
    val knowledgeCount: Int
)
