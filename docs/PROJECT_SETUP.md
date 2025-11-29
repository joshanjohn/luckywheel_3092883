# Lucky Wheel - Project Setup Guide

This guide will help you set up the Lucky Wheel Android application on your local development environment.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation Steps](#installation-steps)
- [Firebase Configuration](#firebase-configuration)
- [Google Authentication Setup](#google-authentication-setup)
- [Building the Project](#building-the-project)
- [Running the App](#running-the-app)
- [Project Structure](#project-structure)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before you begin, ensure you have the following installed:

### Required Software
- **Android Studio**: Ladybug | 2024.2.1 or later
  - Download from: https://developer.android.com/studio
- **JDK**: Java Development Kit 11 or later
- **Git**: For version control
- **Android SDK**: API Level 26 (Android 8.0) minimum, API Level 36 target

### Recommended System Requirements
- **OS**: macOS, Windows 10/11, or Linux
- **RAM**: 8GB minimum, 16GB recommended
- **Disk Space**: 10GB free space minimum
- **Internet Connection**: Required for downloading dependencies and Firebase setup

---

## Installation Steps

### 1. Clone the Repository

```bash
git clone https://github.com/joshanjohn/luckywheel_3092883.git
cd luckywheel_3092883
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select **File → Open**
3. Navigate to the cloned `luckywheel_3092883` directory
4. Click **OK**
5. Wait for Gradle sync to complete (this may take a few minutes on first run)

### 3. Verify Gradle Configuration

The project uses:
- **Gradle Version**: 8.9 (via wrapper)
- **Android Gradle Plugin**: 8.7.3
- **Kotlin Version**: 2.0.0
- **Compile SDK**: 36
- **Min SDK**: 26
- **Target SDK**: 36

---

## Firebase Configuration

### Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **Add project** or select an existing project
3. Follow the setup wizard to create your project

### Step 2: Register Android App

1. In Firebase Console, click **Add app** → **Android**
2. Enter the following details:
   - **Package name**: `com.griffith.luckywheel`
   - **App nickname**: Lucky Wheel (optional)
   - **Debug signing certificate SHA-1**: Required for Google Sign-In
     ```bash
     # Get your SHA-1 certificate
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
3. Click **Register app**

### Step 3: Download Configuration File

1. Download the `google-services.json` file
2. Place it in the `app/` directory of your project:
   ```
   luckywheel_3092883/
   └── app/
       └── google-services.json  ← Place here
   ```

### Step 4: Configure Firebase Realtime Database

1. In Firebase Console, go to **Realtime Database**
2. Click **Create Database**
3. Choose a location (e.g., us-central1)
4. Start in **Test mode** (we'll secure it next)
5. Navigate to the **Rules** tab
6. Copy the contents from `firebase-database-rules.json` in the project root
7. Paste into the rules editor and click **Publish**

For detailed Firebase setup instructions, see [FIREBASE_SETUP.md](../FIREBASE_SETUP.md)

### Step 5: Enable Authentication Methods

1. In Firebase Console, go to **Authentication** → **Sign-in method**
2. Enable the following providers:
   - **Email/Password**: Click Enable
   - **Google**: Click Enable
     - Add your **Web client ID** (from OAuth 2.0 credentials)
     - Add support email

---

## Google Authentication Setup

### Step 1: Create OAuth 2.0 Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project
3. Navigate to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**
5. Select **Web application** as the application type
6. Add authorized redirect URIs (Firebase will provide these)
7. Click **Create**
8. Copy the **Client ID** (it will look like `xxxxx.apps.googleusercontent.com`)

### Step 2: Configure Local Properties

1. Create a `local.properties` file in the project root (if it doesn't exist)
2. Add your Google Web Client ID:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   GOOGLE_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
   ```
3. **Important**: `local.properties` is gitignored for security

### Step 3: Verify Configuration

The app reads the Google Client ID from `local.properties` via `BuildConfig`:
```kotlin
// This is automatically configured in app/build.gradle.kts
buildConfigField(
    "String",
    "GOOGLE_CLIENT_ID",
    "\"${localProperties.getProperty("GOOGLE_CLIENT_ID") ?: ""}\""
)
```

---

## Building the Project

### Using Android Studio

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select **Build → Make Project** (or press `Ctrl+F9` / `Cmd+F9`)
4. Wait for the build to complete

### Using Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run all checks and build
./gradlew build

# Run ktlint code formatting check
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat
```

### Build Output

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

---

## Running the App

### On Physical Device

1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings → Developer Options**
   - Enable **USB Debugging**
3. Connect your device via USB
4. In Android Studio, select your device from the device dropdown
5. Click the **Run** button (green play icon) or press `Shift+F10`

### On Emulator

1. In Android Studio, click **Device Manager**
2. Create a new virtual device:
   - **Device**: Pixel 6 or similar
   - **System Image**: API 34 or higher (with Google APIs)
   - **RAM**: 2GB minimum
3. Click **Run** and select the emulator

### First Run Setup

On first launch, the app will:
1. Show the login/register screen
2. Allow you to create an account or sign in with Google
3. Navigate to the main playground screen

---

## Project Structure

```
luckywheel_3092883/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/griffith/luckywheel/
│   │   │   │   ├── models/          # Data models
│   │   │   │   │   ├── data/        # Data classes (Player, SavedGame, etc.)
│   │   │   │   │   └── enum/        # Enums (SpinActionType)
│   │   │   │   ├── routes/          # Navigation setup
│   │   │   │   ├── services/        # Business logic
│   │   │   │   │   ├── AuthenticationService.kt
│   │   │   │   │   ├── FireBaseService.kt
│   │   │   │   │   └── DataStoreService.kt
│   │   │   │   ├── ui/              # UI components
│   │   │   │   │   ├── screens/     # Screen composables
│   │   │   │   │   └── theme/       # Theme configuration
│   │   │   │   └── utils/           # Utility functions
│   │   │   ├── res/                 # Resources (drawables, values)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Unit tests
│   ├── build.gradle.kts             # App-level build configuration
│   └── google-services.json         # Firebase config (not in repo)
├── docs/                            # Documentation
├── gradle/                          # Gradle wrapper files
├── build.gradle.kts                 # Project-level build configuration
├── settings.gradle.kts              # Gradle settings
├── local.properties                 # Local SDK paths & secrets (not in repo)
├── FIREBASE_SETUP.md                # Firebase setup guide
└── README.md                        # Project overview
```

### Key Technologies

- **UI Framework**: Jetpack Compose
- **Navigation**: Navigation Compose
- **Authentication**: Firebase Auth + Google Sign-In
- **Database**: Firebase Realtime Database
- **Local Storage**: DataStore Preferences
- **Sensors**: Accelerometer for shake detection
- **Architecture**: MVC (Model-View-Controller)

---

## Troubleshooting

### Common Issues

#### 1. Gradle Sync Failed

**Problem**: Gradle sync fails with dependency resolution errors

**Solution**:
```bash
# Clear Gradle cache
./gradlew clean
./gradlew --refresh-dependencies

# In Android Studio: File → Invalidate Caches → Invalidate and Restart
```

#### 2. Google Sign-In Not Working

**Problem**: Google Sign-In button doesn't work or shows errors

**Solutions**:
- Verify `GOOGLE_CLIENT_ID` is set in `local.properties`
- Ensure SHA-1 certificate is added to Firebase Console
- Check that Google Sign-In is enabled in Firebase Authentication
- Verify the Web Client ID (not Android Client ID) is used

#### 3. Firebase Permission Denied

**Problem**: "Permission denied" errors when accessing Firebase

**Solutions**:
- Verify Firebase rules are published (see `FIREBASE_SETUP.md`)
- Ensure user is authenticated before accessing data
- Check that `google-services.json` is in the `app/` directory

#### 4. Build Errors

**Problem**: Compilation errors or missing dependencies

**Solutions**:
```bash
# Clean and rebuild
./gradlew clean build

# Update dependencies
./gradlew --refresh-dependencies
```

#### 5. App Crashes on Launch

**Problem**: App crashes immediately after opening

**Solutions**:
- Check Logcat for error messages
- Verify `google-services.json` is properly configured
- Ensure minimum SDK version is met (API 26+)
- Check that all required permissions are granted

#### 6. Ktlint Formatting Issues

**Problem**: Code formatting doesn't match project standards

**Solution**:
```bash
# Auto-format all code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck
```

### Getting Help

If you encounter issues not covered here:

1. Check the [GitHub Issues](https://github.com/joshanjohn/luckywheel_3092883/issues)
2. Review Firebase Console logs
3. Check Android Studio Logcat for detailed error messages
4. Refer to [FIREBASE_SETUP.md](../FIREBASE_SETUP.md) for database-specific issues

---

## Development Workflow

### Code Style

The project uses **ktlint** for Kotlin code formatting:
```bash
# Format code before committing
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck
```

### CI/CD

The project uses GitHub Actions for continuous integration:
- **Lint Check**: Runs on every pull request
- **Build Verification**: Ensures the app builds successfully
- **Security Scanning**: Mend Bolt for dependency vulnerabilities

### Version Control

- **Main Branch**: `main` (protected)
- **Development**: Create feature branches from `main`
- **Commit Convention**: Use descriptive commit messages

---

## Next Steps

After successful setup:

1. ✅ Run the app on a device/emulator
2. ✅ Create a test account
3. ✅ Test the Gold Wheel feature
4. ✅ Try creating a Custom Wheel
5. ✅ Check the Leaderboard
6. ✅ Test saving and loading games

For more information, see the main [README.md](../README.md)

---

## Additional Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Firebase Documentation](https://firebase.google.com/docs)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)

---

**Happy Coding! 🎉**
