package com.myapplication.platform

/**
 * Android용 서버 URL
 * 에뮬레이터의 경우 10.0.2.2는 호스트의 localhost를 가리킴
 * 실제 기기의 경우 Desktop의 IP 주소를 사용해야 함
 */
actual fun getServerUrl(): String {
    // 에뮬레이터: 10.0.2.2는 호스트의 localhost
    // 실제 기기: Desktop의 IP 주소로 변경 필요 (예: "http://192.168.1.100:8080")
    return "http://10.0.2.2:8080"
}

