package com.myapplication.data

import com.myapplication.platform.FileStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 노트 저장소
 * 로컬 파일 시스템에 JSON 형식으로 노트 저장
 */
class NotesRepository(
    private val fileStorage: FileStorage,
    private val fileName: String = "notes.json"
) {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 저장소 초기화 및 파일에서 노트 로드
     */
    suspend fun loadNotes() {
        try {
            val content = fileStorage.readFile(fileName)
            if (content != null && content.isNotBlank()) {
                val loadedNotes = json.decodeFromString<List<Note>>(content)
                _notes.value = loadedNotes
            } else {
                _notes.value = emptyList()
            }
        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트로 초기화
            _notes.value = emptyList()
        }
    }
    
    /**
     * 노트 목록을 파일에 저장
     */
    private suspend fun saveNotes() {
        try {
            val jsonString = json.encodeToString(_notes.value)
            fileStorage.writeFile(fileName, jsonString)
        } catch (e: Exception) {
            // 예외가 발생하면 호출자에게 전파됨
            throw e
        }
    }
    
    /**
     * 새 노트 생성
     */
    suspend fun createNote(title: String, content: String): Note {
        val newNote = Note.create(title, content)
        _notes.value = _notes.value + newNote
        saveNotes()
        return newNote
    }
    
    /**
     * 노트 업데이트
     */
    suspend fun updateNote(id: String, title: String, content: String): Boolean {
        val noteIndex = _notes.value.indexOfFirst { it.id == id }
        if (noteIndex == -1) return false
        
        val updatedNote = _notes.value[noteIndex].update(title, content)
        _notes.value = _notes.value.toMutableList().apply {
            this[noteIndex] = updatedNote
        }
        saveNotes()
        return true
    }
    
    /**
     * 노트 삭제
     */
    suspend fun deleteNote(id: String): Boolean {
        val noteIndex = _notes.value.indexOfFirst { it.id == id }
        if (noteIndex == -1) return false
        
        _notes.value = _notes.value.filter { it.id != id }
        saveNotes()
        return true
    }
    
    /**
     * ID로 노트 가져오기
     */
    fun getNoteById(id: String): Note? {
        return _notes.value.find { it.id == id }
    }
    
    /**
     * 모든 노트 가져오기
     */
    fun getAllNotes(): List<Note> {
        return _notes.value
    }
}
