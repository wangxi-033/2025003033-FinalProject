package com.example.diary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.viewmodel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewDiaryScreen(
    viewModel: DiaryViewModel,
    entryId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            entry = viewModel.getEntryByIdForView(entryId)
        }
    }

    val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日记详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "编辑") }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        entry?.let { e ->
            Column(
                modifier = Modifier.padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(20.dp)
            ) {
                // 心情
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .then(Modifier.size(48.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text(moodEmoji(e.mood), style = MaterialTheme.typography.headlineMedium) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(e.title, style = MaterialTheme.typography.headlineMedium)
                        Text(dateFormat.format(Date(e.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // 内容
                Text(e.content, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth())

                // 标签
                if (e.tags.isNotBlank()) {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Text("标签", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        e.tags.split(",").filter { it.isNotBlank() }.forEach { tag ->
                            SuggestionChip(
                                onClick = {}, label = { Text(tag.trim()) }
                            )
                        }
                    }
                }
            }
        } ?: run {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("删除后无法恢复，确定要删除这篇日记吗？") },
            confirmButton = {
                TextButton(onClick = {
                    entry?.let { viewModel.deleteEntry(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            }
        )
    }
}
