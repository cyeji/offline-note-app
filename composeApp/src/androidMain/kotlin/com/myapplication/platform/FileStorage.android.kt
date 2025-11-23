package com.myapplication.platform

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android 플랫폼용 파일 저장소 구현
 * 내부 저장소를 사용하여 파일 저장
 * 
 * 주의: expect/actual 패턴에서는 생성자에 파라미터를 전달할 수 없으므로,
 * Application Context를 사용합니다.
 * 
 * server.json은 공유를 위해 외부 저장소(Downloads 폴더)에 저장
 */
actual class FileStorage {
    /**
     * Application Context 가져오기
     */
    private fun getContext(): Context {
        // Application Context를 가져오기
        // 이 방법은 Activity가 없어도 작동합니다
        val contextClass = Class.forName("android.app.ActivityThread")
        val currentApplicationMethod = contextClass.getMethod("currentApplication")
        return currentApplicationMethod.invoke(null) as Context
    }
    
    /**
     * 저장소 디렉토리 경로 가져오기
     * Android의 경우 내부 저장소 사용
     * Context를 가져올 수 없으면 예외를 던져서 호출자가 처리하도록 함
     */
    private fun getStorageDir(): File {
        try {
            val context = getContext()
            // 내부 저장소의 files 디렉토리 사용 (항상 존재함)
            return context.filesDir
        } catch (e: Exception) {
            // Context를 가져올 수 없으면 예외를 던져서 호출자가 처리하도록 함
            throw IllegalStateException("Cannot obtain Android Context for file storage", e)
        }
    }
    
    /**
     * 공유 서버 파일 디렉토리 경로 가져오기
     * Desktop과 동일한 경로를 사용하려고 시도
     * 
     * 주의: 실제로는 Android와 Desktop이 같은 파일 시스템을 공유할 수 없으므로,
     * 네트워크 동기화가 필요합니다. 
     * 
     * 데모 목적으로 Desktop의 홈 디렉토리 경로를 시도하지만,
     * Android에서는 접근할 수 없으므로 내부 저장소를 사용합니다.
     * 실제 동기화를 위해서는 네트워크나 클라우드 저장소가 필요합니다.
     */
    private fun getSharedServerDir(): File {
        try {
            // Desktop과 동일한 경로 시도 (실제로는 작동하지 않을 수 있음)
            val homeDir = System.getProperty("user.home")
            if (homeDir != null) {
                val desktopSharedDir = File(homeDir, ".note-app")
                // Android에서는 Desktop의 홈 디렉토리에 직접 접근할 수 없으므로
                // 내부 저장소를 사용하되, 경로 구조는 동일하게 유지
            }
            
            // Android에서는 내부 저장소의 shared 폴더 사용
            // 실제 동기화를 위해서는 네트워크나 클라우드 저장소 필요
            val context = getContext()
            val sharedDir = File(context.filesDir, "shared")
            if (!sharedDir.exists()) {
                sharedDir.mkdirs()
            }
            return sharedDir
        } catch (e: Exception) {
            // 실패 시 내부 저장소 사용
            val context = getContext()
            val fallbackDir = File(context.filesDir, "shared")
            if (!fallbackDir.exists()) {
                fallbackDir.mkdirs()
            }
            return fallbackDir
        }
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
