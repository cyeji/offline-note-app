package com.myapplication.platform

import io.ktor.client.*

/**
 * 플랫폼별 HTTP 클라이언트 인터페이스
 * expect/actual 패턴을 사용하여 플랫폼별 구현 제공
 */
expect fun createHttpClient(): HttpClient

