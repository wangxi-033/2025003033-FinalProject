package com.example.diary.data.dao

import androidx.room.*
import com.example.diary.data.entity.DiaryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries ORDER BY created_at DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): DiaryEntry?

    @Query("""
        SELECT * FROM diary_entries 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchEntries(query: String): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE mood = :mood ORDER BY created_at DESC")
    fun getEntriesByMood(mood: String): Flow<List<DiaryEntry>>

    @Query("SELECT COUNT(*) FROM diary_entries")
    fun getEntryCount(): Flow<Int>

    @Query("SELECT mood, COUNT(*) as count FROM diary_entries GROUP BY mood")
    fun getMoodStats(): Flow<List<MoodStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry): Long

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)
}

data class MoodStat(
    val mood: String,
    val count: Int
)
