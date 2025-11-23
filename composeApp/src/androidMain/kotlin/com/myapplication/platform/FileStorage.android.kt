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
        // context.filesDir는 항상 존재하므로 디렉토리 확인 불필요
        val file = File(getStorageDir(), fileName)
        file.writeText(content)
        // 예외가 발생하면 호출자에게 전파됨
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
