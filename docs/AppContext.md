# Lucky Wheel Application Context

## Overview
Lucky Wheel is an Android arcade-themed casual gaming application built with Jetpack Compose, Kotlin coroutines, Firebase Authentication, Firebase Realtime Database, and Jetpack DataStore.

## Architecture
- **Language**: Kotlin 2.2.10
- **Build System**: Android Gradle Plugin (AGP) 9.4.1, Gradle Wrapper 9.6.0
- **UI Toolkit**: Jetpack Compose (Material3 + Custom Neon Arcade Design System)
- **State & Storage**: Jetpack DataStore Preferences (`player_pref`)
- **Backend & Auth**: Firebase Auth (Google Sign-In + Email/Password), Firebase Realtime Database
- **Sensors**: Device Accelerometer for shake detection to trigger spins
- **Audio & Services**: BackgroundMusicService for foreground playback, SoundEffectService for SFX

## Key Screens and Components
1. **Authentication (`ui/screens/auth`)**:
   - `LoginScreen`: Handles user login with email/password and Google Sign-In.
   - `RegisterScreen`: Registration flow with input validation.
   - `ForgotPasswordScreen`: Password reset via email.
   - `CustomTextField`: Reusable neon-bordered text input.

2. **Playground (`ui/screens/playground`)**:
   - `GoldWheelScreen`: Physics-based gold wheel with vsync-locked tick loop via `withFrameMillis`.
   - `CustomWheelScreen`: User-customizable wheel with persistent segment configurations.
   - `EditBottomSheet`: Bottom sheet editor for adding, editing, and coloring segments.

3. **Mini Games (`ui/screens/minigames`)**:
   - `CoinFlipScreen`: Arcade-styled best-of-3 coin flip game.

4. **Leaderboard (`ui/screens/leaderboard`)**:
   - `LeaderboardScreen`: Real-time ranking with `PodiumDisplay` for top 3 players and LazyColumn for rankings.

5. **Profile & Settings (`ui/screens/profile`, `ui/screens/settings`)**:
   - `ProfileScreen`: View and manage player profile, gold tally, and account deletion.
   - `SettingsScreen`: Single combined flow (`getAppSettings()`) for audio volumes, mute toggles, and shake sensor switches.

6. **Effects (`ui/components/effects`)**:
   - `MagicalBackground`: Ambient neon particles and shooting stars with pre-allocated reusable canvas paths.
   - `ParticleSystem`: Frame-synchronized particle bursts on wins.

## Release & Signing Setup
- **Application ID**: `com.griffith.luckywheel`
- **Current Version**: `versionCode = 4`, `versionName = "3.1.0"`
- **Target SDK**: 36, **Min SDK**: 26
- **Release Signing**: Configured in `app/build.gradle.kts` using `luckywheel.jks` (`CN=Joshan John`, alias `luckywheel`). Keystore credentials stored locally in `local.properties` (ignored by git).
- **Output Artifact**: `app/build/outputs/bundle/release/app-release.aab`

