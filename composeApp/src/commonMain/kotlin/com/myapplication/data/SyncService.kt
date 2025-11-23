package com.myapplication.data

import com.myapplication.platform.FileStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 동기화 서비스
 * Last-Write-Wins(LWW) 방식으로 로컬과 서버 간 동기화
 * 서버는 로컬 파일 기반 Fake Server (server.json)
 */
class SyncService(
    private val fileStorage: FileStorage,
    private val notesRepository: NotesRepository,
    private val serverFileName: String = "server.json"
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 로컬 노트를 서버로 Push
     */
    suspend fun push(): Result<Unit> {
        return try {
            val localNotes = notesRepository.getAllNotes()
            val jsonString = json.encodeToString(localNotes)
            fileStorage.writeFile(serverFileName, jsonString)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 서버 노트를 로컬로 Pull
     * 충돌 시 updatedAt이 더 최신인 노트를 우선
     */
    suspend fun pull(): Result<Unit> {
        return try {
            val serverContent = fileStorage.readFile(serverFileName)
            if (serverContent == null || serverContent.isBlank()) {
                return Result.success(Unit) // 서버에 데이터가 없으면 성공으로 처리
            }
            
            val serverNotes = json.decodeFromString<List<Note>>(serverContent)
            val localNotes = notesRepository.getAllNotes()
            
            // Last-Write-Wins 방식으로 병합
            val mergedNotes = mergeNotes(localNotes, serverNotes)
            
            // 병합된 노트를 저장소에 반영
            // 직접 저장소의 상태를 업데이트하는 대신, 각 노트를 업데이트하거나 생성
            for (note in mergedNotes) {
                val existingNote = localNotes.find { it.id == note.id }
                if (existingNote == null) {
                    // 새 노트 추가
                    notesRepository.createNote(note.title, note.content)
                } else if (existingNote.updatedAt < note.updatedAt) {
                    // 서버 노트가 더 최신이면 업데이트
                    notesRepository.updateNote(note.id, note.title, note.content)
                }
            }
            
            // 로컬에만 있는 노트는 유지 (서버에 없는 노트)
            // 삭제된 노트는 서버에 없으면 삭제하지 않음 (오프라인-퍼스트)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 로컬과 서버 노트를 병합
     * 같은 ID의 노트가 있으면 updatedAt이 더 최신인 것을 선택
     */
    private fun mergeNotes(localNotes: List<Note>, serverNotes: List<Note>): List<Note> {
        val mergedMap = mutableMapOf<String, Note>()
        
        // 로컬 노트 추가
        localNotes.forEach { note ->
            mergedMap[note.id] = note
        }
        
        // 서버 노트 병합 (충돌 시 updatedAt이 더 최신인 것 선택)
        serverNotes.forEach { serverNote ->
            val localNote = mergedMap[serverNote.id]
            if (localNote == null) {
                // 로컬에 없으면 서버 노트 추가
                mergedMap[serverNote.id] = serverNote
            } else {
                // 둘 다 있으면 updatedAt이 더 최신인 것 선택
                if (serverNote.updatedAt > localNote.updatedAt) {
                    mergedMap[serverNote.id] = serverNote
                }
                // 로컬이 더 최신이면 유지
            }
        }
        
        return mergedMap.values.toList()
    }
    
    /**
     * 동기화 (Push + Pull)
     */
    suspend fun sync(): Result<Unit> {
        return try {
            push().getOrThrow()
            pull().getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
