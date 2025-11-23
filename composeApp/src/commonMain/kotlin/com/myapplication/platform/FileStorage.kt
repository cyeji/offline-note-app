package com.myapplication.platform

/**
 * 플랫폼별 파일 저장소 인터페이스
 * expect/actual 패턴을 사용하여 플랫폼별 구현 제공
 */
expect class FileStorage() {
    /**
     * 파일에서 내용 읽기
     * @param fileName 파일 이름
     * @return 파일 내용, 파일이 없으면 null
     */
    suspend fun readFile(fileName: String): String?
    
    /**
     * 파일에 내용 쓰기
     * @param fileName 파일 이름
     * @param content 저장할 내용
     */
    suspend fun writeFile(fileName: String, content: String)
    
    /**
     * 파일 존재 여부 확인
     * @param fileName 파일 이름
     * @return 파일이 존재하면 true
     */
    suspend fun fileExists(fileName: String): Boolean
}
