# 📝 오프라인-퍼스트 노트 앱  
*Kotlin Multiplatform + Compose Multiplatform 해커톤 템플릿*

이 프로젝트는 **Kotlin Multiplatform(KMP)** 과 **Compose Multiplatform** 을 사용하여  
**Desktop + Android** 환경에서 작동하는 간단한 오프라인-퍼스트 메모장 앱 템플릿입니다.

“낯선 기술 기반 해커톤”이라는 콘셉트를 반영해  
UI/로직을 공유하고, 간단한 Last-Write-Wins(LWW) 방식의 동기화를 지원합니다.

---

## 📌 목적
- Kotlin Multiplatform + Compose 기반의 프로젝트 구조를 빠르게 경험
- Android & Desktop에서 공통 UI/로직 사용하기
- 오프라인에서도 완전히 동작하는 노트 앱 만들기
- 버튼 기반 Sync 구조로 MPP 개발 흐름 익히기

---

## 🚀 핵심 기능

### 1. 노트 CRUD
![img.png](img.png)
- 노트 목록 보기
- 노트 생성/수정/삭제
- 수정 시 자동으로 `updatedAt` 갱신

### 2. 오프라인-퍼스트 저장
- 플랫폼 별 파일 저장소에 JSON 직렬화하여 저장  
  (예: `notes.json`)
- 앱 껐다 켜도 데이터 유지
- 네트워크 필요 없음

### 3. 동기화 버튼 (Last-Write-Wins)
- 상단 "Sync" 버튼 클릭으로 동작
- Push: 로컬 → 서버 (`server.json`)
- Pull: 서버 → 로컬
- 충돌 시 **최신 수정 시각(updatedAt)이 우선**

서버는 실제 서버가 아닌 해커톤 데모용 **로컬 파일 기반 Fake Server** 구조입니다.

**⚠️ 주의사항:**
- Desktop과 Android는 각각 별도의 파일 시스템을 사용하므로, `server.json`이 플랫폼별로 분리되어 있습니다.
- 실제 동기화를 위해서는 네트워크나 클라우드 저장소가 필요합니다.
- 현재 구현은 각 플랫폼 내에서의 동기화만 지원합니다 (로컬 ↔ server.json).

---

## 🛠 기술 스택

### 공통(KMP)
- Kotlin Multiplatform
- Compose Multiplatform
- kotlinx.serialization
- kotlinx.datetime
- File I/O API

### Android
- Jetpack Compose(Android)
- Android Studio / IntelliJ IDEA

### Desktop
- Compose Desktop
- JVM 17+

---

## 📂 프로젝트 구조

```
project-root
├── commonMain
│    ├── data
│    │     ├── Note.kt
│    │     ├── NotesRepository.kt
│    │     ├── SyncService.kt
│    ├── ui
│    │     ├── NoteListScreen.kt
│    │     ├── NoteEditorScreen.kt
│    │     └── App.kt
│
├── androidApp
│    └── MainActivity.kt
│
├── desktopApp
│    └── Main.kt
│
├── notes.json      # 로컬 저장 파일
└── server.json     # Fake 서버 JSON
```

## 프로젝트 실행

### Desktop 실행

터미널에서 다음 명령어를 실행합니다:

```bash
./gradlew :composeApp:run
```

또는 IntelliJ IDEA / Android Studio에서:
1. Gradle 탭 열기
2. `composeApp` → `Tasks` → `compose desktop` → `run` 더블클릭

**파일 저장 위치:** `~/.note-app/notes.json`

---

### Android 실행

#### 필수 조건
- Android SDK 설치 필요
- Android 에뮬레이터 또는 실제 기기 연결

#### 방법 1: Android Studio에서 실행 (권장)
1. Android Studio에서 프로젝트 열기
2. 상단 실행 구성에서 `composeApp` 선택
3. 에뮬레이터 또는 연결된 기기 선택
4. 실행 버튼 클릭

#### 방법 2: 터미널에서 실행

**1단계: Android SDK 경로 설정**
```bash
# local.properties 파일에 SDK 경로 설정 (없으면 자동 생성됨)
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

**2단계: 에뮬레이터 실행 또는 기기 연결**
```bash
# 에뮬레이터 목록 확인
$HOME/Library/Android/sdk/emulator/emulator -list-avds

# 에뮬레이터 실행 (예시)
$HOME/Library/Android/sdk/emulator/emulator -avd <에뮬레이터_이름> &

# 또는 실제 기기 연결 후
$HOME/Library/Android/sdk/platform-tools/adb devices
```

**3단계: 앱 빌드 및 설치**
```bash
# APK 빌드
./gradlew :composeApp:assembleDebug

# 기기/에뮬레이터에 설치
./gradlew :composeApp:installDebug

# 앱 실행
$HOME/Library/Android/sdk/platform-tools/adb shell am start -n com.myapplication/.MainActivity
```

**파일 저장 위치:** `/data/data/com.myapplication/files/notes.json` (내부 저장소)

---
