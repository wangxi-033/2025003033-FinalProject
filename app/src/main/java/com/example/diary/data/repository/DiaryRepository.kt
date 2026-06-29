package com.example.diary.data.repository

import com.example.diary.data.dao.DiaryDao
import com.example.diary.data.entity.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(private val diaryDao: DiaryDao) {

    fun getAllEntries(): Flow<List<DiaryEntry>> = diaryDao.getAllEntries()

    fun searchEntries(query: String): Flow<List<DiaryEntry>> = diaryDao.searchEntries(query)

    fun getEntriesByMood(mood: String): Flow<List<DiaryEntry>> = diaryDao.getEntriesByMood(mood)

    fun getEntryCount(): Flow<Int> = diaryDao.getEntryCount()

    fun getMoodStats() = diaryDao.getMoodStats()

    suspend fun getEntryById(id: Long) = diaryDao.getEntryById(id)

    suspend fun insertEntry(entry: DiaryEntry): Long = diaryDao.insertEntry(entry)

    suspend fun updateEntry(entry: DiaryEntry) = diaryDao.updateEntry(entry)

    suspend fun deleteEntry(entry: DiaryEntry) = diaryDao.deleteEntry(entry)
}
