package com.example.data.db

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MemoryRepository(private val dao: MemoryDao) {

    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemoriesFlow()

    suspend fun addMemoryNote(
        content: String,
        category: MemoryCategory = MemoryCategory.GENERAL,
        importance: Int = 3,
        keywords: String = "",
        source: String = "USER_CONVERSATION"
    ) {
        val entity = MemoryEntity(
            id = UUID.randomUUID().toString(),
            content = content.trim(),
            category = category.name,
            importance = importance.coerceIn(1, 5),
            keywords = keywords.trim(),
            source = source,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMemory(entity)
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return dao.searchMemories(query)
    }

    suspend fun deleteMemoryNote(id: String) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
