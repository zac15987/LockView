# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LockView is an Android application designed to be a lockable image viewer. The project is currently in initial setup phase with basic Android Compose template code. The main functionality described in `LockView-requirement.md` has not been implemented yet.

**Key Details:**
- Language: Kotlin
- UI Framework: Jetpack Compose (Note: Requirements suggest View-based components)
- Build System: Gradle with Kotlin DSL
- Package Name: `com.zac15987.lockview`
- Min SDK: 24 (Android 7.0)
- Target SDK: 36 (Android 15)

## Development Commands

### Building
```bash
cmd.exe /c "gradlew clean build"          # Clean and build everything
cmd.exe /c "gradlew assembleDebug"        # Build debug APK
cmd.exe /c "gradlew assembleRelease"      # Build release APK
cmd.exe /c "gradlew installDebug"         # Build and install debug APK on connected device
cmd.exe /c "gradlew bundle"              # Assemble bundles for all variants
```

### Testing
```bash
cmd.exe /c "gradlew test"                 # Run unit tests for all variants
cmd.exe /c "gradlew testDebugUnitTest"    # Run unit tests for debug build
cmd.exe /c "gradlew connectedAndroidTest" # Run instrumentation tests on connected devices
cmd.exe /c "gradlew connectedDebugAndroidTest" # Run debug instrumentation tests
cmd.exe /c "gradlew check"               # Run all verification tasks
```

### Code Quality
```bash
cmd.exe /c "gradlew lint"                # Run lint on the default variant
cmd.exe /c "gradlew lintDebug"          # Run lint for debug variant
cmd.exe /c "gradlew lintRelease"        # Run lint for release variant
cmd.exe /c "gradlew lintFix"            # Run lint and apply safe suggestions
cmd.exe /c "gradlew updateLintBaseline"  # Update lint baseline
```

### Other Useful Commands
```bash
cmd.exe /c "gradlew tasks"              # List main tasks
cmd.exe /c "gradlew tasks --all"        # List all available tasks
cmd.exe /c "gradlew dependencies"       # Show project dependencies
cmd.exe /c "gradlew signingReport"      # Display signing info
cmd.exe /c "gradlew uninstallDebug"     # Uninstall debug build from device
```

**Note:** use cmd.exe /c "gradlew" or cmd.exe /c "gradlew.bat"

## Architecture & Implementation Notes

### Current State vs Requirements
The project has a fundamental mismatch between its current setup and requirements:
- **Current**: Jetpack Compose UI framework
- **Required**: View-based components (PhotoView library)

### Required Dependencies Not Yet Added
From `LockView-requirement.md`, these need to be added to `app/build.gradle.kts`:
- Glide for image loading
- PhotoView for zoom/pan functionality
- Material Components

### Missing Permissions
Add to `AndroidManifest.xml`:
- `READ_EXTERNAL_STORAGE`
- `READ_MEDIA_IMAGES` (for Android 13+)

### Core Features to Implement
1. **Image Selection**: Using ActivityResultContracts or Intent.ACTION_PICK
2. **Image Display**: PhotoView with zoom/pan gestures
3. **Lock Mechanism**: Disable touch events with visual indicator
4. **Unlock Methods**: Triple tap, long press, pattern, volume buttons, or shake

### Suggested Package Structure
```
com.zac15987.lockview/
├── MainActivity.kt
├── ui/
│   ├── LockablePhotoView.kt
│   └── UnlockGestureDetector.kt
├── utils/
│   ├── ImageLoader.kt
│   └── PermissionHelper.kt
└── models/
    └── ImageState.kt
```

## Key Implementation Considerations

### State Management
- Save zoom level, pan position, and lock state
- Handle configuration changes (rotation)
- Use `onSaveInstanceState()` and `onRestoreInstanceState()`

### Gesture Requirements
- Pinch zoom: 0.5x to 5x scale
- Double tap: Toggle fit/2x zoom
- Lock state must disable all image interactions
- Multiple unlock methods should be implemented

### Testing Focus Areas
- Various screen sizes and densities
- Gesture recognition accuracy
- State persistence
- Different image formats and sizes
- Permission handling edge cases