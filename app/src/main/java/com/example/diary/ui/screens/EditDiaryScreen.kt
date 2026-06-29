package com.example.diary.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diary.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryScreen(
    viewModel: DiaryViewModel,
    entryId: Long?,
    onBack: () -> Unit
) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(entryId) {
        if (entryId != null) {
            viewModel.startEditEntry(entryId)
        } else {
            viewModel.startNewEntry()
        }
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) onBack()
    }

    val moods = listOf(
        "happy" to "😊 开心", "sad" to "😢 难过", "neutral" to "😐 平静",
        "excited" to "🎉 兴奋", "angry" to "😠 生气", "calm" to "😌 放松"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (formState.isEditing) "编辑日记" else "写日记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveEntry() }) {
                        Icon(Icons.Default.Check, "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            OutlinedTextField(
                value = formState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("标题") },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.titleError != null,
                supportingText = formState.titleError?.let { { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 心情选择
            Text("今日心情", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { (mood, label) ->
                    FilterChip(
                        selected = formState.mood == mood,
                        onClick = { viewModel.updateMood(mood) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            // 内容
            OutlinedTextField(
                value = formState.content,
                onValueChange = { viewModel.updateContent(it) },
                label = { Text("内容") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 20
            )

            // 标签
            OutlinedTextField(
                value = formState.tags,
                onValueChange = { viewModel.updateTags(it) },
                label = { Text("标签（逗号分隔）") },
                placeholder = { Text("如：学习,生活,感悟") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // 保存按钮
            Button(
                onClick = { viewModel.saveEntry() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("保存日记") }
        }
    }
}
