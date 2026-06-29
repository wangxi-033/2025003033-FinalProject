package com.example.diary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.diary.data.database.DiaryDatabase
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.data.network.RetrofitClient
import com.example.diary.data.repository.DiaryRepository
import com.example.diary.data.repository.QuoteRepository
import com.example.diary.datastore.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val diaryDao = DiaryDatabase.getDatabase(application).diaryDao()
    private val diaryRepository = DiaryRepository(diaryDao)
    private val quoteRepository = QuoteRepository(RetrofitClient.apiService)
    private val userPreferences = UserPreferences(application)

    // 日记列表
    @OptIn(ExperimentalCoroutinesApi::class)
    val diaryListState: StateFlow<DiaryListState> = diaryRepository.getAllEntries()
        .map { entries -> DiaryListState.Success(entries) as DiaryListState }
        .catch { e -> emit(DiaryListState.Error(e.message ?: "加载失败")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryListState.Loading)

    // 名言
    private val _quoteState = MutableStateFlow<QuoteState>(QuoteState.Loading)
    val quoteState: StateFlow<QuoteState> = _quoteState.asStateFlow()

    // 搜索
    private val _searchQuery = MutableStateFlow("")
    private val _selectedMood = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredEntries: StateFlow<DiaryListState> = combine(
        _searchQuery, _selectedMood
    ) { query, mood ->
        Pair(query, mood)
    }.flatMapLatest { (query, mood) ->
        when {
            query.isNotBlank() -> diaryRepository.searchEntries(query)
            mood != null -> diaryRepository.getEntriesByMood(mood)
            else -> diaryRepository.getAllEntries()
        }
    }.map { entries -> DiaryListState.Success(entries) as DiaryListState }
        .catch { e -> emit(DiaryListState.Error(e.message ?: "加载失败")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DiaryListState.Loading)

    // 表单
    private val _formState = MutableStateFlow(DiaryFormState())
    val formState: StateFlow<DiaryFormState> = _formState.asStateFlow()

    // 默认心情
    val defaultMood: StateFlow<String> = userPreferences.defaultMood
        .stateIn(viewModelScope, SharingStarted.Eagerly, "neutral")

    init {
        fetchQuote()
    }

    fun fetchQuote() {
        viewModelScope.launch {
            _quoteState.value = QuoteState.Loading
            quoteRepository.getRandomQuote().fold(
                onSuccess = { dto ->
                    _quoteState.value = QuoteState.Success(
                        content = dto.content,
                        author = dto.author
                    )
                },
                onFailure = { e ->
                    _quoteState.value = QuoteState.Error(e.message ?: "网络错误")
                }
            )
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterByMood(mood: String?) {
        _selectedMood.value = mood
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedMood.value = null
    }

    fun updateTitle(title: String) {
        _formState.update { it.copy(title = title, titleError = null, isSaved = false) }
    }

    fun updateContent(content: String) {
        _formState.update { it.copy(content = content, isSaved = false) }
    }

    fun updateMood(mood: String) {
        _formState.update { it.copy(mood = mood, isSaved = false) }
    }

    fun updateTags(tags: String) {
        _formState.update { it.copy(tags = tags, isSaved = false) }
    }

    fun startNewEntry() {
        viewModelScope.launch {
            val mood = userPreferences.defaultMood.first()
            _formState.value = DiaryFormState(mood = mood, isEditing = false)
        }
    }

    fun startEditEntry(id: Long) {
        viewModelScope.launch {
            val entry = diaryRepository.getEntryById(id)
            entry?.let {
                _formState.value = DiaryFormState(
                    title = it.title,
                    content = it.content,
                    mood = it.mood,
                    tags = it.tags,
                    isEditing = true,
                    editingId = it.id
                )
            }
        }
    }

    fun saveEntry() {
        val state = _formState.value
        if (state.title.isBlank()) {
            _formState.update { it.copy(titleError = "标题不能为空") }
            return
        }

        viewModelScope.launch {
            val entry = DiaryEntry(
                id = if (state.isEditing) state.editingId!! else 0,
                title = state.title.trim(),
                content = state.content.trim(),
                mood = state.mood,
                tags = state.tags.trim(),
                createdAt = if (state.isEditing) 0 else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            if (state.isEditing) {
                diaryRepository.updateEntry(entry)
            } else {
                diaryRepository.insertEntry(entry)
            }
            _formState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            diaryRepository.deleteEntry(entry)
        }
    }

    fun saveDefaultMood(mood: String) {
        viewModelScope.launch {
            userPreferences.setDefaultMood(mood)
        }
    }

    suspend fun getEntryByIdForView(id: Long): DiaryEntry? {
        return diaryRepository.getEntryById(id)
    }
}
