package com.example.diary.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "diary_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_SORT_ORDER = stringPreferencesKey("sort_order")
        val KEY_DEFAULT_MOOD = stringPreferencesKey("default_mood")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_DEFAULT_TAG = stringPreferencesKey("default_tag")
    }

    val sortOrder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SORT_ORDER] ?: "time_desc"
    }

    val defaultMood: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_MOOD] ?: "neutral"
    }

    val defaultTag: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_TAG] ?: ""
    }

    suspend fun setSortOrder(order: String) {
        context.dataStore.edit { it[KEY_SORT_ORDER] = order }
    }

    suspend fun setDefaultMood(mood: String) {
        context.dataStore.edit { it[KEY_DEFAULT_MOOD] = mood }
    }

    suspend fun setDefaultTag(tag: String) {
        context.dataStore.edit { it[KEY_DEFAULT_TAG] = tag }
    }
}
