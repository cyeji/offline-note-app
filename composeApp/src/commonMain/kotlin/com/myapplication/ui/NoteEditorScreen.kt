package com.myapplication.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.myapplication.data.NotesRepository
import kotlinx.coroutines.launch

/**
 * 노트 편집 화면
 * @param noteId 노트 ID (빈 문자열이면 새 노트 생성)
 * @param onBack 클릭 시 목록으로 돌아가기
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String,
    notesRepository: NotesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val existingNote = remember(noteId) {
        if (noteId.isNotBlank()) {
            notesRepository.getNoteById(noteId)
        } else {
            null
        }
    }
    
    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // 포커스 요청자 (새 노트 생성 시 제목 필드에 자동 포커스)
    val titleFocusRequester = remember { FocusRequester() }
    
    // 새 노트 생성 시 제목 필드에 자동 포커스
    LaunchedEffect(existingNote) {
        if (existingNote == null) {
            titleFocusRequester.requestFocus()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingNote == null) "새 노트" else "노트 편집") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                scope.launch {
                                    isSaving = true
                                    errorMessage = null
                                    try {
                                        if (existingNote == null) {
                                            // 새 노트 생성
                                            notesRepository.createNote(title, content)
                                        } else {
                                            // 기존 노트 업데이트
                                            notesRepository.updateNote(noteId, title, content)
                                        }
                                        isSaving = false
                                        onBack()
                                    } catch (e: Exception) {
                                        isSaving = false
                                        errorMessage = "저장 실패: ${e.message ?: "알 수 없는 오류"}"
                                        e.printStackTrace()
                                    }
                                }
                            }
                        },
                        enabled = !isSaving && (title.isNotBlank() || content.isNotBlank())
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "저장")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 에러 메시지 표시
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                
                // 3초 후 메시지 자동 제거
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(3000)
                    errorMessage = null
                }
            }
            
            // 제목 입력 필드
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                placeholder = { Text("노트 제목을 입력하세요") },
                enabled = !isSaving,
                readOnly = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .focusRequester(titleFocusRequester),
                singleLine = true
            )
            
            // 내용 입력 필드
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                placeholder = { Text("노트 내용을 입력하세요") },
                enabled = !isSaving,
                readOnly = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                minLines = 10,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}
