package com.myapplication.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myapplication.data.Note
import com.myapplication.data.NotesRepository
import com.myapplication.data.SyncService
import kotlinx.coroutines.launch

/**
 * 노트 목록 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    notesRepository: NotesRepository,
    syncService: SyncService,
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by notesRepository.notes.collectAsState()
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }
    
    // 초기 로드는 App.kt에서 서버에서 Pull하므로 여기서는 불필요
    // LaunchedEffect(Unit) {
    //     notesRepository.loadNotes()
    // }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("노트 앱") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncing = true
                                syncMessage = null
                                val result = syncService.sync()
                                isSyncing = false
                                if (result.isSuccess) {
                                    syncMessage = "동기화 완료"
                                    notesRepository.loadNotes() // 동기화 후 목록 새로고침
                                } else {
                                    syncMessage = "동기화 실패: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        enabled = !isSyncing
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "동기화")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNoteClick("") } // 빈 ID는 새 노트 생성
            ) {
                Icon(Icons.Default.Add, contentDescription = "노트 추가")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 동기화 메시지 표시
            syncMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("실패")) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 3초 후 메시지 자동 제거
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    syncMessage = null
                }
            }
            
            if (notes.isEmpty()) {
                // 빈 목록 표시
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "노트가 없습니다",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+ 버튼을 눌러 새 노트를 추가하세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 노트 목록 표시
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notes.sortedByDescending { it.updatedAt },
                        key = { it.id }
                    ) { note ->
                        NoteItem(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = {
                                scope.launch {
                                    notesRepository.deleteNote(note.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 노트 목록 아이템
 */
@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = note.title.ifBlank { "제목 없음" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = note.content.take(100).ifBlank { "내용 없음" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatTimestamp(note.updatedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 타임스탬프를 읽기 쉬운 형식으로 변환
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "방금 전"
        diff < 3600_000 -> "${diff / 60_000}분 전"
        diff < 86400_000 -> "${diff / 3600_000}시간 전"
        diff < 604800_000 -> "${diff / 86400_000}일 전"
        else -> {
            // 간단한 날짜 포맷팅 (YYYY-MM-DD)
            val date = java.util.Date(timestamp)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = date
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
        }
    }
}
