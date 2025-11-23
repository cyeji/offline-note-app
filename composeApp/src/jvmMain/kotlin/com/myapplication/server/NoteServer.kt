package com.myapplication.server

import com.myapplication.data.Note
import com.myapplication.platform.FileStorage
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.encodeToString
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * 노트 동기화를 위한 간단한 HTTP 서버
 * Desktop에서 실행되어 Android와 데이터를 공유
 */
class NoteServer(
    private val fileStorage: FileStorage,
    private val port: Int = 8080
) {
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * 서버 시작
     */
    suspend fun start() {
        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json()
            }
            
            routing {
                // GET /notes - 서버의 노트 목록 가져오기
                get("/notes") {
                    val notes = loadNotesFromServer()
                    call.respond(notes)
                }
                
                // POST /notes - 서버에 노트 목록 저장하기
                post("/notes") {
                    val notes = call.receive<List<Note>>()
                    saveNotesToServer(notes)
                    call.respond(mapOf("success" to true))
                }
                
                // GET /health - 서버 상태 확인
                get("/health") {
                    call.respond(mapOf("status" to "ok"))
                }
            }
        }.start(wait = false)
    }
    
    /**
     * 서버 중지
     */
    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }
    
    /**
     * 서버에서 노트 목록 로드
     */
    private suspend fun loadNotesFromServer(): List<Note> {
        return withContext(Dispatchers.IO) {
            try {
                val content = fileStorage.readFile("server.json")
                if (content == null || content.isBlank()) {
                    emptyList()
                } else {
                    json.decodeFromString<List<Note>>(content)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * 서버에 노트 목록 저장
     */
    private suspend fun saveNotesToServer(notes: List<Note>) {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(notes)
                fileStorage.writeFile("server.json", content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

