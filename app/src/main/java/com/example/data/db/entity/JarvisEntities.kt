package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "core_memory",
    indices = [Index(value = ["memoryKey"], unique = true)]
)
data class CoreMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memoryKey: String,
    val memoryValue: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["timestamp"])]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "BOSS" or "JARVIS"
    val content: String,
    val languageCode: String,
    val brainUsed: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "facts",
    indices = [Index(value = ["subject", "factContent"], unique = true)]
)
data class FactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val predicate: String = "is",
    val factContent: String,
    val category: String = "General",
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "preferences",
    indices = [Index(value = ["prefKey"], unique = true)]
)
data class PreferenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prefKey: String,
    val prefValue: String,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["isCompleted", "timestamp"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: Int = 1, // 1=Normal, 2=High, 3=Urgent
    val dueDate: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "contacts_memory",
    indices = [Index(value = ["name"])]
)
data class ContactMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String = "", // e.g. "Mom", "Doctor", "Manager"
    val nickname: String = "",
    val callCount: Int = 0,
    val lastContacted: Long = 0
)

@Entity(
    tableName = "knowledge",
    indices = [Index(value = ["title"])]
)
data class KnowledgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_logs",
    indices = [Index(value = ["timestamp"])]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionName: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val authorized: Boolean,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
