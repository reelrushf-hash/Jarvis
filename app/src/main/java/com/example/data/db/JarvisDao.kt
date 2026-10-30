package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.AuditLogEntity
import com.example.data.db.entity.ContactMemoryEntity
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.CoreMemoryEntity
import com.example.data.db.entity.FactEntity
import com.example.data.db.entity.KnowledgeEntity
import com.example.data.db.entity.PreferenceEntity
import com.example.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JarvisDao {

    // --- Core Memory ---
    @Query("SELECT * FROM core_memory WHERE memoryKey = :key LIMIT 1")
    suspend fun getCoreMemory(key: String): CoreMemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCoreMemory(entity: CoreMemoryEntity)

    // --- Conversations ---
    @Insert
    suspend fun insertConversation(item: ConversationEntity): Long

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentConversations(limit: Int = 40): Flow<List<ConversationEntity>>

    @Query("SELECT COUNT(*) FROM conversations")
    suspend fun getConversationCount(): Int

    @Query("DELETE FROM conversations WHERE id NOT IN (SELECT id FROM conversations ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun pruneOldConversations(keepCount: Int = 100)

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()

    // --- Facts ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: FactEntity): Long

    @Query("SELECT * FROM facts ORDER BY timestamp DESC LIMIT :limit")
    fun getAllFacts(limit: Int = 50): Flow<List<FactEntity>>

    @Query("SELECT * FROM facts WHERE subject LIKE '%' || :query || '%' OR factContent LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchFacts(query: String): List<FactEntity>

    @Query("SELECT COUNT(*) FROM facts")
    suspend fun getFactCount(): Int

    @Query("DELETE FROM facts WHERE id = :id")
    suspend fun deleteFact(id: Long)

    // --- Preferences ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(pref: PreferenceEntity)

    @Query("SELECT * FROM preferences WHERE prefKey = :key LIMIT 1")
    suspend fun getPreference(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences ORDER BY timestamp DESC LIMIT :limit")
    fun getAllPreferences(limit: Int = 50): Flow<List<PreferenceEntity>>

    // --- Tasks ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, timestamp DESC LIMIT :limit")
    fun getAllTasks(limit: Int = 50): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, timestamp DESC LIMIT 30")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    // --- Contacts Memory ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactMemoryEntity): Long

    @Query("SELECT * FROM contacts_memory ORDER BY callCount DESC, name ASC LIMIT :limit")
    fun getAllContacts(limit: Int = 50): Flow<List<ContactMemoryEntity>>

    @Query("SELECT * FROM contacts_memory WHERE name LIKE '%' || :query || '%' OR relationship LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchContacts(query: String): List<ContactMemoryEntity>

    @Query("UPDATE contacts_memory SET callCount = callCount + 1, lastContacted = :timestamp WHERE id = :id")
    suspend fun recordCall(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM contacts_memory")
    suspend fun getContactCount(): Int

    @Query("DELETE FROM contacts_memory WHERE id = :id")
    suspend fun deleteContact(id: Long)

    // --- Knowledge ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(knowledge: KnowledgeEntity): Long

    @Query("SELECT * FROM knowledge ORDER BY timestamp DESC LIMIT :limit")
    fun getAllKnowledge(limit: Int = 50): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' LIMIT 20")
    suspend fun searchKnowledge(query: String): List<KnowledgeEntity>

    @Query("SELECT COUNT(*) FROM knowledge")
    suspend fun getKnowledgeCount(): Int

    @Query("DELETE FROM knowledge WHERE id = :id")
    suspend fun deleteKnowledge(id: Long)

    // --- Audit Logs ---
    @Insert
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditLogs(limit: Int = 30): Flow<List<AuditLogEntity>>

    // --- Global Wipe ---
    @Query("DELETE FROM facts")
    suspend fun clearFacts()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM knowledge")
    suspend fun clearKnowledge()

    @Query("DELETE FROM contacts_memory")
    suspend fun clearContacts()
}
