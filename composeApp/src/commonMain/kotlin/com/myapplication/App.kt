package com.myapplication

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.myapplication.data.NotesRepository
import com.myapplication.data.SyncService
import com.myapplication.platform.FileStorage
import com.myapplication.ui.NoteEditorScreen
import com.myapplication.ui.NoteListScreen

/**
 * 메인 앱 컴포저블
 * 노트 목록과 편집 화면 간 네비게이션 관리
 */
@Composable
fun App() {
    MaterialTheme {
        // 파일 저장소 및 저장소 초기화
        val fileStorage = remember { FileStorage() }
        val notesRepository = remember { NotesRepository(fileStorage) }
        val syncService = remember { SyncService(fileStorage, notesRepository) }
        val scope = rememberCoroutineScope()
        
        // 자동 동기화 설정: 변경사항 발생 시 즉시 Push + Pull
        LaunchedEffect(notesRepository, syncService) {
            notesRepository.onNotesChanged = {
                // 변경사항 발생 시 서버로 Push하고 즉시 Pull
                try {
                    syncService.push()
                    // Push 후 즉시 Pull하여 서버의 최신 데이터 반영
                    kotlinx.coroutines.delay(100) // Push 완료 대기
                    syncService.pull()
                } catch (e: Exception) {
                    // 동기화 실패는 무시 (오프라인 모드)
                    e.printStackTrace()
                }
            }
        }
        
        // 주기적으로 서버에서 Pull (1초마다) - 실시간 동기화
        LaunchedEffect(syncService) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val result = syncService.pull()
                result.onFailure { e ->
                    // Pull 실패 로그 출력
                    e.printStackTrace()
                }
            }
        }
        
        // 앱 시작 시 서버에서 초기 데이터 로드
        LaunchedEffect(syncService) {
            try {
                syncService.pull()
            } catch (e: Exception) {
                // 실패 시 로컬 파일에서 로드
                notesRepository.loadNotes()
            }
        }
        
        // 네비게이션 상태 관리
        var currentScreen by remember { mutableStateOf<Screen>(Screen.NoteList) }
        var selectedNoteId by remember { mutableStateOf<String?>(null) }
        
        when (currentScreen) {
            is Screen.NoteList -> {
                NoteListScreen(
                    notesRepository = notesRepository,
                    syncService = syncService,
                    onNoteClick = { noteId ->
                        selectedNoteId = noteId
                        currentScreen = Screen.NoteEditor(noteId)
                    }
                )
            }
            is Screen.NoteEditor -> {
                NoteEditorScreen(
                    noteId = selectedNoteId ?: "",
                    notesRepository = notesRepository,
                    onBack = {
                        currentScreen = Screen.NoteList
                        selectedNoteId = null
                    }
                )
            }
        }
    }
}

/**
 * 화면 상태를 나타내는 sealed class
 */
sealed class Screen {
    object NoteList : Screen()
    data class NoteEditor(val noteId: String) : Screen()
}