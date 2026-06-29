package com.example.diary.data.dao

import androidx.room.*
import com.example.diary.data.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY useCount DESC")
    fun getAllTags(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag)

    @Query("UPDATE tags SET useCount = useCount + 1 WHERE name = :name")
    suspend fun incrementUseCount(name: String)
}
