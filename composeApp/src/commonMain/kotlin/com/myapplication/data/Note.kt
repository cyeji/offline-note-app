package com.myapplication.data

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * 노트 데이터 모델
 * @param id 노트 고유 식별자
 * @param title 노트 제목
 * @param content 노트 내용
 * @param createdAt 생성 시각
 * @param updatedAt 마지막 수정 시각
 */
@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long, // Unix timestamp (milliseconds)
    val updatedAt: Long  // Unix timestamp (milliseconds)
) {
    /**
     * 노트를 업데이트하고 updatedAt을 현재 시각으로 갱신
     */
    fun update(title: String, content: String): Note {
        return copy(
            title = title,
            content = content,
            updatedAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        )
    }
    
    companion object {
        /**
         * 새 노트 생성
         */
        fun create(title: String, content: String): Note {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            return Note(
                id = generateId(),
                title = title,
                content = content,
                createdAt = now,
                updatedAt = now
            )
        }
        
        /**
         * 고유 ID 생성
         * 타임스탬프와 랜덤 숫자를 조합하여 충돌 가능성을 최소화
         */
        private fun generateId(): String {
            val timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val random = (0..999999).random() // 충돌 가능성 감소를 위해 범위 확대
            return "$timestamp-$random"
        }
    }
}
