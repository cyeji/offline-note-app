# 📋 노트 앱 개발 작업 계획서

## 프로젝트 개요
Kotlin Multiplatform + Compose Multiplatform을 사용한 오프라인-퍼스트 노트 앱 개발

---

## 📌 작업 단계별 진행 상황

### 1단계: 프로젝트 설정 및 의존성 추가 ✅

#### 1.1 필요한 라이브러리 추가
- [x] `kotlinx.serialization` 추가 (JSON 직렬화)
- [x] `kotlinx.datetime` 추가 (타임스탬프 관리)
- [x] 플랫폼별 파일 I/O 지원 확인

#### 1.2 프로젝트 구조 생성
- [x] `data/` 디렉토리 생성
- [x] `ui/` 디렉토리 생성
- [x] `platform/` 디렉토리 생성

---

### 2단계: 데이터 모델 구현 ✅

#### 2.1 `Note.kt` 생성
- [x] Note 데이터 클래스 생성
- [x] 필드: `id`, `title`, `content`, `createdAt`, `updatedAt`
- [x] `@Serializable` 어노테이션 적용
- [x] `updatedAt` 자동 갱신 로직 포함

---

### 3단계: 파일 저장소 구현 (플랫폼별) ✅

#### 3.1 `FileStorage.kt` (expect) 생성
- [x] 공통 인터페이스 정의
- [x] 메서드: `readFile()`, `writeFile()`, `fileExists()`

#### 3.2 Android 구현 (`FileStorage.android.kt`)
- [x] Context를 사용한 파일 저장소 구현
- [x] 내부 저장소에 `notes.json` 저장

#### 3.3 Desktop 구현 (`FileStorage.desktop.kt`)
- [x] JVM 파일 시스템 API 사용
- [x] 프로젝트 루트 또는 사용자 디렉토리에 저장

---

### 4단계: 저장소 계층 구현 ✅

#### 4.1 `NotesRepository.kt` 생성
- [x] CRUD 메서드 구현
- [x] JSON 직렬화/역직렬화 로직
- [x] 파일 저장소 연동
- [x] `updatedAt` 자동 갱신 로직

---

### 5단계: 동기화 서비스 구현 ✅

#### 5.1 `SyncService.kt` 생성
- [x] Last-Write-Wins(LWW) 로직 구현
- [x] Push: 로컬 → `server.json`
- [x] Pull: `server.json` → 로컬
- [x] 충돌 시 `updatedAt` 비교로 해결
- [x] Fake 서버는 로컬 파일 기반

---

### 6단계: UI 컴포넌트 구현

#### 6.1 `NoteListScreen.kt` 생성
- [ ] 노트 목록 표시
- [ ] FloatingActionButton으로 노트 생성
- [ ] 노트 클릭 시 편집 화면 이동
- [ ] 삭제 기능 (스와이프 또는 버튼)
- [ ] Sync 버튼 (상단)

#### 6.2 `NoteEditorScreen.kt` 생성
- [ ] 제목/내용 편집 UI
- [ ] 저장/취소 버튼
- [ ] 새 노트 생성 모드
- [ ] 기존 노트 수정 모드

#### 6.3 `App.kt` 수정
- [ ] 네비게이션 구조 구현
- [ ] `NoteListScreen`을 메인 화면으로 설정
- [ ] 상태 관리 (ViewModel 또는 State)

---

### 7단계: 플랫폼별 진입점 확인

#### 7.1 Android
- [ ] `MainActivity.kt` 확인 및 수정
- [ ] `FileStorage`에 Context 전달

#### 7.2 Desktop
- [ ] `jvmMain/main.kt` 확인 및 수정
- [ ] 파일 경로 설정

---

### 8단계: 테스트 및 검증

#### 8.1 기능 테스트
- [ ] 노트 생성/수정/삭제 테스트
- [ ] 앱 재시작 후 데이터 유지 확인
- [ ] 동기화 기능 테스트
- [ ] 충돌 해결 테스트

#### 8.2 플랫폼별 테스트
- [ ] Android에서 실행 테스트
- [ ] Desktop에서 실행 테스트
- [ ] 양쪽에서 동일 동작 확인

---

## 📝 구현 세부사항

### 기술 스택
- Kotlin Multiplatform
- Compose Multiplatform
- kotlinx.serialization
- kotlinx.datetime
- File I/O API

### 파일 구조
```
commonMain/kotlin/com/myapplication/
├── data/
│   ├── Note.kt
│   ├── NotesRepository.kt
│   └── SyncService.kt
├── ui/
│   ├── NoteListScreen.kt
│   ├── NoteEditorScreen.kt
│   └── App.kt
└── platform/
    ├── FileStorage.kt (expect)
    ├── android/FileStorage.android.kt (actual)
    └── desktop/FileStorage.desktop.kt (actual)
```

### 주요 기능
1. 노트 CRUD (생성, 읽기, 수정, 삭제)
2. 오프라인-퍼스트 저장 (JSON 파일 기반)
3. 동기화 버튼 (Last-Write-Wins 방식)

---

## 🔄 진행 상황 업데이트 로그

- **2024-12-XX**: 작업 계획서 생성
- **2024-12-XX**: 1단계 완료 - 프로젝트 설정 및 의존성 추가 (kotlinx.serialization, kotlinx.datetime, Material Icons)
- **2024-12-XX**: 2단계 완료 - 데이터 모델 구현 (Note.kt)
- **2024-12-XX**: 3단계 완료 - 플랫폼별 파일 저장소 구현 (FileStorage expect/actual)
- **2024-12-XX**: 4단계 완료 - 저장소 계층 구현 (NotesRepository)
- **2024-12-XX**: 5단계 완료 - 동기화 서비스 구현 (SyncService with LWW)
