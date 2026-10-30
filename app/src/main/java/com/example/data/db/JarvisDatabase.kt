package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.entity.AuditLogEntity
import com.example.data.db.entity.ContactMemoryEntity
import com.example.data.db.entity.ConversationEntity
import com.example.data.db.entity.CoreMemoryEntity
import com.example.data.db.entity.FactEntity
import com.example.data.db.entity.KnowledgeEntity
import com.example.data.db.entity.PreferenceEntity
import com.example.data.db.entity.TaskEntity

@Database(
    entities = [
        CoreMemoryEntity::class,
        ConversationEntity::class,
        FactEntity::class,
        PreferenceEntity::class,
        TaskEntity::class,
        ContactMemoryEntity::class,
        KnowledgeEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {

    abstract fun jarvisDao(): JarvisDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getDatabase(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_memory.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
