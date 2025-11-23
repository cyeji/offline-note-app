package com.myapplication.data

import com.myapplication.platform.FileStorage
import com.myapplication.platform.createHttpClient
import com.myapplication.platform.getServerUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 동기화 서비스
 * Last-Write-Wins(LWW) 방식으로 로컬과 서버 간 동기화
 * HTTP 서버를 통해 Desktop과 Android 간 데이터 공유
 */
class SyncService(
    private val fileStorage: FileStorage,
    private val notesRepository: NotesRepository,
    private val serverUrl: String = getServerUrl()
) {
    private val httpClient: HttpClient = createHttpClient()
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
            println("SyncService: Pushing ${localNotes.size} notes to $serverUrl/notes")
            
            // HTTP 서버로 전송 시도
            try {
                val response = httpClient.post("$serverUrl/notes") {
                    contentType(ContentType.Application.Json)
                    setBody(localNotes)
                }
                println("SyncService: Successfully pushed to server")
                // 성공적으로 전송됨
                Result.success(Unit)
            } catch (e: Exception) {
                println("SyncService: Failed to push to server: ${e.message}")
                e.printStackTrace()
                // 서버에 연결할 수 없으면 로컬 파일에 저장 (오프라인 모드)
                val jsonString = json.encodeToString(localNotes)
                fileStorage.writeFile("server.json", jsonString)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            println("SyncService: Push error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 서버 노트를 로컬로 Pull
     * 서버를 단일 소스로 사용 - 서버 데이터를 그대로 반영
     */
    suspend fun pull(): Result<Unit> {
        return try {
            val serverNotes: List<Note> = try {
                // HTTP 서버에서 가져오기 시도
                println("SyncService: Pulling from $serverUrl/notes")
                val response = httpClient.get("$serverUrl/notes")
                val notes = response.body<List<Note>>()
                println("SyncService: Pulled ${notes.size} notes from server")
                notes
            } catch (e: Exception) {
                // 서버에 연결할 수 없으면 로컬 파일에서 읽기 (오프라인 모드)
                println("SyncService: Failed to pull from server: ${e.message}")
                e.printStackTrace()
                val serverContent = fileStorage.readFile("server.json")
                if (serverContent == null || serverContent.isBlank()) {
                    emptyList() // 서버에 데이터가 없으면 빈 리스트 반환
                } else {
                    json.decodeFromString<List<Note>>(serverContent)
                }
            }
            
            // 서버 노트를 그대로 반영 (서버가 단일 소스)
            println("SyncService: Replacing notes with ${serverNotes.size} server notes")
            notesRepository.replaceAllNotes(serverNotes)
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * 로컬과 서버 노트를 병합 (수동 Sync용)
     * 같은 ID의 노트가 있으면 updatedAt이 더 최신인 것을 선택
     */
    private fun mergeNotes(localNotes: List<Note>, serverNotes: List<Note>): List<Note> {
        val mergedMap = mutableMapOf<String, Note>()
        
        // 서버 노트를 우선 (서버가 단일 소스)
        serverNotes.forEach { note ->
            mergedMap[note.id] = note
        }
        
        // 로컬에만 있는 노트 추가 (서버에 없는 경우)
        localNotes.forEach { note ->
            if (!mergedMap.containsKey(note.id)) {
                mergedMap[note.id] = note
            } else {
                // 둘 다 있으면 updatedAt이 더 최신인 것 선택
                val serverNote = mergedMap[note.id]!!
                if (note.updatedAt > serverNote.updatedAt) {
                    mergedMap[note.id] = note
                }
            }
        }
        
        return mergedMap.values.toList()
    }
    
    /**
     * 동기화 (Push + Pull)
     * 수동 Sync 버튼용 - 병합 로직 사용
     */
    suspend fun sync(): Result<Unit> {
        return try {
            // 먼저 Push
            push().getOrThrow()
            
            // 서버에서 가져오기
            val serverNotes: List<Note> = try {
                httpClient.get("$serverUrl/notes").body()
            } catch (e: Exception) {
                val serverContent = fileStorage.readFile("server.json")
                if (serverContent == null || serverContent.isBlank()) {
                    emptyList()
                } else {
                    json.decodeFromString<List<Note>>(serverContent)
                }
            }
            
            val localNotes = notesRepository.getAllNotes()
            
            // 병합 로직 사용 (수동 Sync는 병합)
            val mergedNotes = mergeNotes(localNotes, serverNotes)
            notesRepository.replaceAllNotes(mergedNotes)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
