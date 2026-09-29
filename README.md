# Roy Music — Premium Android Music Player

Roy Music is a modern, high-fidelity offline music player designed and built with Jetpack Compose, Material 3, AndroidX Media3 (ExoPlayer & MediaSessionService), Room Database, and Preferences DataStore.

---

## Key Features

1. **Intelligent Mood-Based Organization**
   - 10 Built-in Mood Categories: *Happy, Sad, Romantic, Energetic, Calm, Focus, Workout, Party, Sleep, Devotional*
   - Transparent, automated mood recommendations based on metadata and folder tags
   - Create custom moods with personalized colors and descriptions
   - One-tap "Play All" and "Shuffle" for any mood
   - Assign songs, entire albums, or folders to moods

2. **Automatic Favorites System**
   - Intelligently recognizes listening habits: counts plays when a song is listened to for ≥ 70% or ≥ 60 seconds
   - Automatically promotes songs to Favorites after reaching the qualifying threshold (configurable, default: 5 plays)
   - Displays reasons for auto-added songs with options to undo or exclude
   - Fully customizable rules in Settings

3. **Folder-Based Music Browser**
   - Hierarchical folder navigation for local audio files
   - Total folder track count and aggregate duration
   - Search within folders
   - Instant folder playback, shuffling, and playlist creation
   - Multi-format support: MP3, FLAC, WAV, AAC, M4A, OGG, Opus

4. **Comprehensive Music Library**
   - **All Songs**: Complete indexed tracks
   - **Playlists**: Full creation, reordering, and track management
   - **Albums & Artists**: Art-forward grid views and grouped discography
   - **Favorites**: Filtered and sorted by recent, play count, title, or artist
   - **Global Search**: Search by title, artist, album, folder, and mood tags with search history

5. **Advanced Music Player & Audio Effects**
   - Fluid album artwork with customizable corner geometry (Rounded, Squircle, Circular)
   - Real-time animated equalizer bars during playback
   - Precise scrub slider, 10s skip controls, repeat modes, and shuffle
   - Playback speed control (0.5x to 2.0x)
   - Sleep timer with configurable countdown
   - 5-Band Audio Equalizer with 8 genre presets (*Normal, Rock, Pop, Classical, Jazz, Bass Boost, Vocal, Electronic*)
   - Bass Boost and 3D Virtualizer enhancements
   - Plain text & editable lyrics viewer

6. **8 Premium Color Themes & Customization**
   - Midnight Black (Default)
   - Deep Purple
   - Ocean Blue
   - Emerald Green
   - Sunset Orange
   - Rose Pink
   - AMOLED Black (Pure black for OLED displays)
   - Light Minimal
   - 3 Mini-Player styles: *Floating Card, Compact Bar, Glass Banner*
   - Immediate theme application without application restarts

7. **100% Privacy & Offline Operation**
   - No mandatory account or sign-in required
   - All playback history, stats, and metadata are stored exclusively on-device in SQLite Room database
   - Foreground playback service with system lockscreen and notification controls

---

## Architecture & Tech Stack

- **UI**: 100% Jetpack Compose with Material Design 3
- **Audio Engine**: AndroidX Media3 ExoPlayer (`1.5.1`) with `MediaSessionService`
- **Database**: AndroidX Room (`2.7.0`) with KSP code generator
- **Preferences**: AndroidX Preferences DataStore (`1.1.7`)
- **Image Loading**: Coil Compose (`2.7.0`)
- **Concurrency**: Kotlin Coroutines & reactive Flow architecture

---

## Build & Run Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34+ (compileSdk: 36)

### How to Build in Android Studio
1. Open Android Studio and select **Open** -> Select the project root folder.
2. Let Gradle sync project dependencies.
3. Connect an Android device (Android 8.0 / API 26+) or launch an Android Virtual Device (AVD).
4. Click **Run** (`Shift + F10`) to build and launch the debug APK.

### Generate a Signed Release APK
1. In Android Studio, go to **Build** -> **Generate Signed Bundle / APK...**
2. Choose **APK** -> Click **Next**.
3. Select your keystore path, enter passwords and key alias.
4. Select build variant `release` and check `V1 (Jar Signature)` and `V2 (Full APK Signature)`.
5. Click **Finish**. The output APK will be placed in `app/release/`.

---

## Developer & Contact Information

**Roy Music** is proudly developed and maintained by:

- **Developer:** Abhinaba Roy Pradhan
- **Phone:** +91 6295869078
- **Email:** abhinabapradhan@gmail.com

For inquiries, collaborations, feature suggestions, or feedback, feel free to get in touch!

