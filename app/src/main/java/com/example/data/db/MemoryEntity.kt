package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemoryCategory {
    FACT,
    PREFERENCE,
    PROJECT,
    LINGUISTIC,
    EPISODIC,
    GENERAL;

    companion object {
        fun fromString(str: String): MemoryCategory {
            return try {
                valueOf(str.uppercase().trim())
            } catch (_: Exception) {
                GENERAL
            }
        }
    }
}

@Entity(tableName = "memory_notes")
data class MemoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val content: String,
    val category: String = "GENERAL",
    val importance: Int = 3,
    val keywords: String = "",
    val source: String = "USER_CONVERSATION",
    val timestamp: Long = System.currentTimeMillis()
)
