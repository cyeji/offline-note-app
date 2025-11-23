package com.myapplication.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop (JVM) 플랫폼용 파일 저장소 구현
 * 사용자 홈 디렉토리의 .note-app 폴더에 파일 저장
 * 
 * server.json은 공유를 위해 홈 디렉토리의 .note-app 폴더에 저장
 */
actual class FileStorage {
    /**
     * 저장소 디렉토리 경로 가져오기
     * 사용자 홈 디렉토리의 .note-app 폴더 사용
     */
    private fun getStorageDir(): File {
        val homeDir = System.getProperty("user.home") ?: "."
        val storageDir = File(homeDir, ".note-app")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        return storageDir
    }
    
    /**
     * 공유 서버 파일 디렉토리 경로 가져오기
     * Desktop과 Android가 모두 접근 가능한 공유 위치
     * 현재는 Desktop의 홈 디렉토리 사용 (실제로는 네트워크 동기화 필요)
     */
    private fun getSharedServerDir(): File {
        val homeDir = System.getProperty("user.home") ?: "."
        val sharedDir = File(homeDir, ".note-app")
        if (!sharedDir.exists()) {
            sharedDir.mkdirs()
        }
        return sharedDir
    }
    
    actual suspend fun readFile(fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            // server.json은 공유 디렉토리에서 읽기
            val dir = if (fileName == "server.json") getSharedServerDir() else getStorageDir()
            val file = File(dir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    actual suspend fun writeFile(fileName: String, content: String) = withContext(Dispatchers.IO) {
        // server.json은 공유 디렉토리에 쓰기
        val dir = if (fileName == "server.json") getSharedServerDir() else getStorageDir()
        val file = File(dir, fileName)
        file.writeText(content)
        // 예외가 발생하면 호출자에게 전파됨
    }
    
    actual suspend fun fileExists(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // server.json은 공유 디렉토리에서 확인
            val dir = if (fileName == "server.json") getSharedServerDir() else getStorageDir()
            val file = File(dir, fileName)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
}
