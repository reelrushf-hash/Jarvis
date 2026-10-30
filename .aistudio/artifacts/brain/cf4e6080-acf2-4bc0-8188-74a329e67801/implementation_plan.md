# JARVIS AI — BOSS EDITION (Lightweight Advanced Upgrade V2)

Transform the existing JARVIS AI codebase into an ultra-lightweight, battery-efficient, and privacy-focused personal assistant tailored for low-spec Android devices (particularly Redmi and Xiaomi devices with 2 GB RAM), featuring a minimal dark interface, an optional floating overlay button, and on-demand offline/online intelligence.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and user experience decisions have been aligned based on user preferences and 2 GB RAM hardware constraints:
>
> - **Confirmed UI Direction**: A minimal, distraction-free home screen featuring a single centered cyan circular JARVIS voice button, a live recognized command line, a compact response bubble, a status pill, and a top-right settings icon opening a slide-out drawer/sheet for advanced tools (Memory Vault, System Telemetry, and Boss Security).
> - **Confirmed Floating Overlay Mode**: An opt-in compact movable floating button enabled via Settings with `SYSTEM_ALERT_WINDOW` permission, terminating all background polling and audio recording as soon as tasks conclude.
> - **Performance & Battery Baseline**: Event-driven audio lifecycle without CPU wake-locks, bounded Room queries (max 30–50 items), lazy initialization of speech and AI components, and R8/ProGuard shrinking configured for minimal APK and memory footprints.

---

## 1. Overview & Core Concept

**JARVIS AI — Boss Edition V2** is built specifically for users requiring a responsive, loyal AI personal assistant on budget hardware. It runs completely offline for all daily utility tasks—phone calls, application launching, notes, to-dos, local memory recall, and bioscience reference—while seamlessly delegating complex queries to Gemini AI when network connectivity and RAM headroom permit.

- **Primary Persona**: "Boss" (the authenticated owner of the device).
- **Core Languages**: Hindi, English, and Bengali (বাংলা) with native script recognition and localized text-to-speech.
- **Hardware Profile**: Android 7.0+ (API 24+) with specific tuning for 2 GB RAM phones (e.g., Redmi 9A/10A, Xiaomi Redmi Go/A-series) where memory exhaustion and aggressive background killing are prevalent.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Zero-State / Idle Screen**:
   - Deep obsidian background (`#03070E`) with no heavy background bitmaps or constant animations.
   - Header with discreet "J.A.R.V.I.S." badge, connection status pill (`OFFLINE SHIELD` / `HYBRID ONLINE`), and a compact Settings gear icon.
   - Central interactive **Cyan Circular JARVIS Button** (minimum 80dp touch target, accessible with TalkBack).
   - "At your command, Boss" idle prompt with quick suggestions in the active language.
2. **Push-to-Talk Execution**:
   - Tapping the circular button initiates `SpeechRecognizer` in the configured language (`en-US`, `hi-IN`, or `bn-IN`).
   - A single, subtle cyan listening wave or ring outline pulses only while active.
   - Live partial transcription displays in real-time.
   - On completion, `OfflineBrain` or `OnlineBrain` parses the intent, triggers the action, displays a concise response card, and speaks aloud using Android TTS.
3. **Emergency Stop Flow**:
   - Global red "ABORT" pill or spoken command (`"stop"`, `"abort"`, `"रुक जाओ"`, `"থামো"`) immediately silences TTS, kills speech capture, and aborts any pending action.
4. **Settings & Advanced Vault Drawer**:
   - Tapping the settings gear reveals a clean, modal bottom sheet containing:
     - **500 MB Memory Vault**: Indexed local database stats, facts, tasks, and pinned contacts.
     - **Device Telemetry**: Real-time available RAM vs. total RAM, battery health, and 2 GB Low-RAM switch.
     - **Boss Credentials**: PIN configuration (`1122`), name, and TTS pitch/speed sliders.
     - **Floating Assistant**: Opt-in toggle to launch the movable screen overlay.

---

## 3. Key Product Decisions & Trade-Offs

- **Minimalist Vector Visuals vs. Heavy Asset Animations**:
  - *Decision*: Replace canvas loop ticks and raster background images in routine drawing with a lightweight vector-rendered cyan circle with simple Compose state animations.
  - *Why*: Eliminates GPU overdraw, prevents frame drops on low-end Mali/Adreno GPUs, and saves battery.
- **R8 Code & Resource Shrinking**:
  - *Decision*: Enable `isMinifyEnabled = true` and `isShrinkResources = true` in `release` build type with fine-tuned ProGuard rules for Room, Coroutines, and Moshi.
  - *Why*: Drops unused classes and reduces APK size significantly.
- **Event-Driven Audio vs. Always-On Mic**:
  - *Decision*: Push-to-talk as the strict default; no background audio recording without user tap.
  - *Why*: Prevents battery drain and respects privacy policies on untrusted ambient noise.
- **Floating Overlay Service**:
  - *Decision*: Implement a lightweight `FloatingJarvisService` using Android's `WindowManager` and `SYSTEM_ALERT_WINDOW`, with immediate shutdown on task completion.
  - *Why*: Allows Boss to invoke JARVIS over any running app (WhatsApp, YouTube, Browser) while remaining low-RAM compliant.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                   MAIN ACTIVITY UI                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │   Minimal HUD: Header, Cyan Mic, Response Card   │  │
│  └────────────────────────┬─────────────────────────┘  │
│                           │                             │
│       ┌───────────────────┴───────────────────┐        │
│       ▼                                       ▼        │
│  Settings Drawer                     Floating Service  │
│  (Vault, Telemetry, PIN)             (Overlay Window)  │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
                  JARVIS VIEW MODEL
                            │
       ┌────────────────────┼────────────────────┐
       ▼                    ▼                    ▼
 VoiceEngine          BrainRouter         SystemActions
 (STT & TTS)          (Offline/Online)    (Calls, Apps, WA)
                            │
       ┌────────────────────┴────────────────────┐
       ▼                                         ▼
 OfflineBrain                              OnlineBrain
 (Regex, Multilingual Parser,              (Firebase AI /
  Local Bioscience Knowledge)               Gemini 2.5)
       │
       ▼
 JarvisMemoryRepository
 ┌───────────────────────────────────────────────┐
 │ Room SQLite (500 MB Configurable Budget Vault) │
 │ - CoreMemory (Boss Profile & PIN)             │
 │ - ConversationMemory (Bounded Pruned Logs)    │
 │ - FactMemory (Deduplicated Knowledge)         │
 │ - TaskMemory & ContactMemory                  │
 └───────────────────────────────────────────────┘
```

---

## 5. Execution Steps

1. **Gradle & APK Optimization**:
   - Configure R8 code and resource shrinking in `app/build.gradle.kts`.
   - Remove unused test dependencies or unnecessary heavy libraries.
2. **UI Simplification (V2 Minimal HUD)**:
   - Streamline `MainHudScreen.kt` to the minimal dark layout with single cyan button, transcript, and response area.
   - Refactor `MainActivity.kt` to replace multi-tab bottom navigation with a clean modal settings drawer/sheet.
3. **Floating Assistant Overlay Service**:
   - Implement `FloatingJarvisService` with movable touch listener, mic button, and clean dismiss.
   - Declare the service in `AndroidManifest.xml` with overlay permission handling.
4. **Low RAM & Battery Guardrails**:
   - Add low-memory callback handling in `MainActivity` (`onTrimMemory`).
   - Ensure `VoiceEngine` cleanly tears down audio buffers and unbinds listeners immediately after utterance completion.
5. **Verification**:
   - Run `compile_applet` and local unit tests (`testDebugUnitTest`).
   - Validate APK size and performance metrics.
