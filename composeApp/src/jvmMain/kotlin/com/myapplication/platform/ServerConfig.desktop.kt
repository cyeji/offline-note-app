package com.myapplication.platform

/**
 * Desktop용 서버 URL
 * Desktop에서는 localhost 사용
 */
actual fun getServerUrl(): String {
    return "http://localhost:8080"
}

