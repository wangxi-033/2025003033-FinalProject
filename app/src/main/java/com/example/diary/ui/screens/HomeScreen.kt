package com.example.diary.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.ui.theme.*
import com.example.diary.viewmodel.DiaryListState
import com.example.diary.viewmodel.DiaryViewModel
import com.example.diary.viewmodel.QuoteState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,
    onEntryClick: (DiaryEntry) -> Unit,
    onAddClick: () -> Unit
) {
    val diaryState by viewModel.filteredEntries.collectAsState()
    val quoteState by viewModel.quoteState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showMoodFilter by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📔 心情日记") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { showMoodFilter = !showMoodFilter }) {
                        Icon(Icons.Default.FilterList, "筛选", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "写日记", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 名言卡片
            QuoteCard(quoteState = quoteState, onRefresh = { viewModel.fetchQuote() })

            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.search(it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("搜索日记...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; viewModel.clearFilters() }) {
                            Icon(Icons.Default.Close, "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 心情筛选
            AnimatedVisibility(visible = showMoodFilter) {
                MoodFilterBar(
                    selectedMood = selectedMood,
                    onMoodSelected = { mood ->
                        if (mood == selectedMood) {
                            selectedMood = null
                            viewModel.clearFilters()
                        } else {
                            selectedMood = mood
                            viewModel.filterByMood(mood)
                        }
                    }
                )
            }

            // 日记列表
            when (val state = diaryState) {
                is DiaryListState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                is DiaryListState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("加载失败: ${state.message}", color = MaterialTheme.colorScheme.error) }
                is DiaryListState.Success -> {
                    if (state.entries.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotEmpty()) "没有找到相关日记" else "还没有日记\n点击 + 开始写吧！"
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.entries, key = { it.id }) { entry ->
                                DiaryCard(entry = entry, onClick = { onEntryClick(entry) },
                                    onDelete = { viewModel.deleteEntry(entry) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuoteCard(quoteState: QuoteState, onRefresh: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text("每日一言", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                Spacer(Modifier.height(4.dp))
                when (quoteState) {
                    is QuoteState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp), strokeWidth = 2.dp
                    )
                    is QuoteState.Error -> Text("加载失败，下拉刷新",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error)
                    is QuoteState.Success -> {
                        Text(quoteState.content,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic)
                        Spacer(Modifier.height(4.dp))
                        Text("— ${quoteState.author}",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, "刷新", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun MoodFilterBar(selectedMood: String?, onMoodSelected: (String) -> Unit) {
    val moods = listOf("happy" to "😊", "sad" to "😢", "neutral" to "😐",
        "excited" to "🎉", "angry" to "😠", "calm" to "😌")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        moods.forEach { (mood, emoji) ->
            val selected = mood == selectedMood
            AssistChip(
                onClick = { onMoodSelected(mood) },
                label = { Text(emoji) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun DiaryCard(entry: DiaryEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape)
                    .background(moodColor(entry.mood).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) { Text(moodEmoji(entry.mood), fontSize = MaterialTheme.typography.titleLarge.fontSize) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(entry.content, style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dateFormat.format(Date(entry.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📝", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}

fun moodEmoji(mood: String): String = when (mood) {
    "happy" -> "😊"; "sad" -> "😢"; "excited" -> "🎉"
    "angry" -> "😠"; "calm" -> "😌"; else -> "😐"
}

fun moodColor(mood: String) = when (mood) {
    "happy" -> MoodHappy; "sad" -> MoodSad; "excited" -> MoodExcited
    "angry" -> MoodAngry; "calm" -> MoodCalm; else -> MoodNeutral
}
