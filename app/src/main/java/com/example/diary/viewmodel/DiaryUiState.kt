package com.example.diary.viewmodel

import com.example.diary.data.entity.DiaryEntry

sealed interface DiaryListState {
    data object Loading : DiaryListState
    data class Success(val entries: List<DiaryEntry>) : DiaryListState
    data class Error(val message: String) : DiaryListState
}

sealed interface QuoteState {
    data object Loading : QuoteState
    data class Success(val content: String, val author: String) : QuoteState
    data class Error(val message: String) : QuoteState
}

data class DiaryFormState(
    val title: String = "",
    val content: String = "",
    val mood: String = "neutral",
    val tags: String = "",
    val isEditing: Boolean = false,
    val editingId: Long? = null,
    val titleError: String? = null,
    val isSaved: Boolean = false
)
