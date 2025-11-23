package com.myapplication.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop (JVM) 플랫폼용 파일 저장소 구현
 * 사용자 홈 디렉토리의 .note-app 폴더에 파일 저장
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
    
    actual suspend fun readFile(fileName: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(getStorageDir(), fileName)
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
        try {
            val dir = getStorageDir()
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, fileName)
            file.writeText(content)
        } catch (e: Exception) {
            // 에러 처리 (나중에 로깅 추가)
        }
    }
    
    actual suspend fun fileExists(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(getStorageDir(), fileName)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
}
