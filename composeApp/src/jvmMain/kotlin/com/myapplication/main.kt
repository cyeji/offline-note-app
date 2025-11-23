package com.myapplication

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.myapplication.platform.FileStorage
import com.myapplication.server.NoteServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main() = application {
    // HTTP 서버 시작
    val fileStorage = FileStorage()
    val server = NoteServer(fileStorage, port = 8080)
    
    CoroutineScope(Dispatchers.IO).launch {
        server.start()
        println("노트 동기화 서버가 시작되었습니다: http://localhost:8080")
    }
    
    Window(
        onCloseRequest = {
            server.stop()
            exitApplication()
        },
        title = "note-app",
    ) {
        App()
    }
}