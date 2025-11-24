package com.myapplication.data

import com.myapplication.platform.FileStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * NotesRepository 테스트
 */
class NotesRepositoryTest {
    
    @Test
    fun `빈 저장소에서 노트 생성`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-create.json")
        
        val note = repository.createNote("제목", "내용")
        
        assertEquals("제목", note.title)
        assertEquals("내용", note.content)
        assertEquals(1, repository.getAllNotes().size)
        assertNotNull(repository.getNoteById(note.id))
    }
    
    @Test
    fun `노트 업데이트`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-update.json")
        
        // 초기화: 빈 상태에서 시작
        repository.loadNotes()
        
        val note = repository.createNote("원본 제목", "원본 내용")
        val success = repository.updateNote(note.id, "수정된 제목", "수정된 내용")
        
        assertTrue(success)
        val updatedNote = repository.getNoteById(note.id)
        assertNotNull(updatedNote)
        assertEquals("수정된 제목", updatedNote.title)
        assertEquals("수정된 내용", updatedNote.content)
        assertTrue(updatedNote.updatedAt > note.updatedAt)
    }
    
    @Test
    fun `존재하지 않는 노트 업데이트 실패`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-update-fail.json")
        
        val success = repository.updateNote("존재하지 않는 ID", "제목", "내용")
        
        assertFalse(success)
    }
    
    @Test
    fun `노트 삭제`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-delete.json")
        
        val note1 = repository.createNote("제목1", "내용1")
        val note2 = repository.createNote("제목2", "내용2")
        
        assertEquals(2, repository.getAllNotes().size)
        
        val success = repository.deleteNote(note1.id)
        
        assertTrue(success)
        assertEquals(1, repository.getAllNotes().size)
        assertNull(repository.getNoteById(note1.id))
        assertNotNull(repository.getNoteById(note2.id))
    }
    
    @Test
    fun `존재하지 않는 노트 삭제 실패`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-delete-fail.json")
        
        val success = repository.deleteNote("존재하지 않는 ID")
        
        assertFalse(success)
    }
    
    @Test
    fun `파일에서 노트 로드`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-load.json")
        
        // 노트 생성하여 파일에 저장
        repository.createNote("제목1", "내용1")
        repository.createNote("제목2", "내용2")
        
        // 새 저장소 인스턴스 생성하여 파일에서 로드
        val newRepository = NotesRepository(fileStorage, "test-load.json")
        newRepository.loadNotes()
        
        val loadedNotes = newRepository.getAllNotes()
        assertEquals(2, loadedNotes.size)
        assertEquals("제목1", loadedNotes[0].title)
        assertEquals("제목2", loadedNotes[1].title)
    }
    
    @Test
    fun `빈 파일에서 로드 시 빈 리스트 반환`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-load-empty.json")
        
        repository.loadNotes()
        
        assertEquals(0, repository.getAllNotes().size)
    }
    
    @Test
    fun `replaceAllNotes로 전체 교체`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-replace.json")
        
        repository.createNote("원본1", "내용1")
        repository.createNote("원본2", "내용2")
        
        val newNotes = listOf(
            Note.create("새1", "내용1"),
            Note.create("새2", "내용2"),
            Note.create("새3", "내용3")
        )
        
        repository.replaceAllNotes(newNotes)
        
        val allNotes = repository.getAllNotes()
        assertEquals(3, allNotes.size)
        assertEquals("새1", allNotes[0].title)
        assertEquals("새2", allNotes[1].title)
        assertEquals("새3", allNotes[2].title)
    }
    
    @Test
    fun `getAllNotes는 현재 상태 반환`() = runTest {
        val fileStorage = FileStorage()
        val repository = NotesRepository(fileStorage, "test-getall.json")
        
        assertEquals(0, repository.getAllNotes().size)
        
        repository.createNote("제목1", "내용1")
        assertEquals(1, repository.getAllNotes().size)
        
        repository.createNote("제목2", "내용2")
        assertEquals(2, repository.getAllNotes().size)
    }
}

