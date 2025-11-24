package com.myapplication.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Note 데이터 모델 테스트
 */
class NoteTest {
    
    @Test
    fun `Note 생성 시 createdAt과 updatedAt이 동일해야 함`() {
        val note = Note.create("테스트 제목", "테스트 내용")
        
        assertEquals("테스트 제목", note.title)
        assertEquals("테스트 내용", note.content)
        assertEquals(note.createdAt, note.updatedAt)
        assertTrue(note.id.isNotBlank())
    }
    
    @Test
    fun `Note update 시 updatedAt이 변경되어야 함`() {
        val originalNote = Note.create("원본 제목", "원본 내용")
        val originalUpdatedAt = originalNote.updatedAt
        
        // 약간의 지연을 주어 시간 차이를 확실히 함
        Thread.sleep(10)
        
        val updatedNote = originalNote.update("수정된 제목", "수정된 내용")
        
        assertEquals("수정된 제목", updatedNote.title)
        assertEquals("수정된 내용", updatedNote.content)
        assertEquals(originalNote.id, updatedNote.id)
        assertEquals(originalNote.createdAt, updatedNote.createdAt)
        assertTrue(updatedNote.updatedAt > originalUpdatedAt)
    }
    
    @Test
    fun `Note create 시 고유 ID가 생성되어야 함`() {
        val note1 = Note.create("제목1", "내용1")
        val note2 = Note.create("제목2", "내용2")
        
        assertNotEquals(note1.id, note2.id)
    }
    
    @Test
    fun `Note update 시 다른 필드는 유지되어야 함`() {
        val originalNote = Note.create("제목", "내용")
        val originalId = originalNote.id
        val originalCreatedAt = originalNote.createdAt
        
        val updatedNote = originalNote.update("새 제목", "새 내용")
        
        assertEquals(originalId, updatedNote.id)
        assertEquals(originalCreatedAt, updatedNote.createdAt)
    }
}


