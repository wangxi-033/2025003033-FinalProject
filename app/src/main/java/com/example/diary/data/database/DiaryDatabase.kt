package com.example.diary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.diary.data.dao.DiaryDao
import com.example.diary.data.dao.TagDao
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.data.entity.Tag

@Database(entities = [DiaryEntry::class, Tag::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    abstract fun tagDao(): TagDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
