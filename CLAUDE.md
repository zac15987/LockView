# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LockView is a fully functional Android application that provides a lockable image viewer with advanced gesture controls. The app uses modern Android architecture with Jetpack Compose and implements comprehensive zoom, pan, and unlock functionality.

**Key Details:**
- Language: Kotlin
- UI Framework: Jetpack Compose with Material3
- Architecture: MVVM with StateFlow
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

## Architecture Overview

### MVVM Architecture with Compose
The app follows modern Android architecture patterns:
- **State Management**: `ImageViewerViewModel` with `StateFlow`
- **UI Layer**: Declarative Compose UI with state-driven updates
- **Data Layer**: `ImageViewerState` data class for centralized state
- **Gesture Handling**: Custom Compose modifiers and gesture detectors

### Key Dependencies
- **Jetpack Compose**: UI framework with Material3
- **Coil Compose**: Image loading and caching
- **Accompanist Permissions**: Modern permission handling
- **AndroidX Lifecycle**: ViewModel and state management

### Package Structure
```
com.zac15987.lockview/
├── MainActivity.kt
├── data/
│   └── ImageViewerState.kt          # State data model
├── ui/
│   ├── components/
│   │   └── ZoomableImage.kt         # Custom zoomable image component
│   └── screens/
│       └── ImageViewerScreen.kt     # Main UI screen
├── utils/
│   └── PermissionHandler.kt         # Permission utilities
└── viewmodel/
    └── ImageViewerViewModel.kt      # State and business logic
```

## Core Features

### Image Handling
- Image selection using `ActivityResultContracts.GetContent()`
- Coil-based image loading with error handling and loading states
- Support for various image formats

### Zoom and Pan Controls
- Pinch zoom with scale constraints
- Pan gestures with boundary detection
- Double-tap zoom toggle (fit-to-screen vs 2x zoom)
- Smooth animations and state persistence

### Lock Mechanism
- Visual lock indicator with Material3 design
- Complete gesture disabling when locked
- Lock state persists through configuration changes

### Unlock Methods
- **Phone Screen Unlock**: Automatic unlock when device screen is unlocked

### State Management
- Zoom level and pan position persistence
- Lock state maintained across rotations
- Configuration change handling via ViewModel

## Development Notes

### Testing Areas Needing Expansion
- Gesture recognition accuracy tests
- State persistence validation
- Image loading error scenarios
- Permission edge cases

### Permissions Configuration
Permissions are properly configured in `AndroidManifest.xml`:
- `READ_EXTERNAL_STORAGE` (for Android 12 and below)
- `READ_MEDIA_IMAGES` (for Android 13+)

### State Flow Architecture
The app uses modern reactive state management:
- `ImageViewerViewModel.uiState` exposes `StateFlow<ImageViewerState>`
- UI automatically recomposes when state changes
- Unidirectional data flow with clear action methods