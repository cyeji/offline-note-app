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