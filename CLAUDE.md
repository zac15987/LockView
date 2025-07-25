# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

LockView is a fully functional Android application that provides a lockable image viewer with advanced gesture controls, theme customization, and automatic device-unlock integration. The app uses modern Android architecture with Jetpack Compose and implements comprehensive zoom, pan, unlock functionality, and persistent user preferences.

**Key Details:**
- Language: Kotlin
- UI Framework: Jetpack Compose with Material3
- Architecture: MVVM with StateFlow + Repository Pattern
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
- **DataStore Preferences**: Persistent user preferences (theme and language settings)
- **Material3 Dynamic Color**: Adaptive theming for Android 12+

### Package Structure
```
com.zac15987.lockview/
├── LockViewApplication.kt          # Custom Application class for locale initialization
├── MainActivity.kt                 # Main activity with device unlock integration
├── data/
│   ├── ImageViewerState.kt         # Image viewer state data model
│   ├── Bounds.kt                   # Boundary calculation utilities
│   ├── language/
│   │   ├── LanguagePreference.kt   # Language preference enum
│   │   └── LanguageRepository.kt   # Language persistence with DataStore + SharedPreferences
│   └── theme/
│       ├── ThemePreference.kt      # Theme preference enum
│       └── ThemeRepository.kt      # Theme persistence with DataStore
├── ui/
│   ├── components/
│   │   ├── ZoomableImage.kt        # Zoomable image with gesture handling
│   │   ├── TransformGestureDetector.kt # Custom gesture detection
│   │   └── LicensesDialog.kt       # Open source licenses and about dialog
│   ├── screens/
│   │   └── ImageViewerScreen.kt    # Main UI screen with theme and language pickers
│   └── theme/
│       ├── Theme.kt                # Material3 theme with dynamic theming
│       ├── Color.kt                # Color palette definitions
│       ├── Type.kt                 # Typography definitions
│       └── LocaleProvider.kt       # Compose context provider for immediate language switching
├── utils/
│   ├── PermissionHandler.kt        # Permission utilities
│   └── LocaleHelper.kt             # Locale configuration and system language reset
└── viewmodel/
    ├── ImageViewerViewModel.kt     # Image state and business logic
    ├── SettingsViewModel.kt        # Combined theme and language state management
    ├── SettingsViewModelFactory.kt # Factory for SettingsViewModel dependency injection
    ├── ThemeViewModel.kt           # Legacy theme state management (still used)
    └── ThemeViewModelFactory.kt    # Legacy theme ViewModel factory
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
- Enhanced toast messages with usage tips

### Unlock Methods
- **Device Screen Unlock**: Automatic unlock via broadcast receivers (`ACTION_USER_PRESENT`)
- **App Resume Unlock**: KeyguardManager integration for seamless unlocking

### Theme System
- **Light/Dark/System themes**: User-configurable theme preference
- **Dynamic theming**: Material You integration on Android 12+
- **DataStore persistence**: Theme settings survive app restarts
- **Theme-aware components**: All UI elements adapt to theme changes

### Internationalization System
- **Multi-language support**: English, Traditional Chinese, and System (follows device language)
- **Immediate language switching**: Changes apply instantly without app restart using `LocaleProvider`
- **Dual persistence**: DataStore for reactive UI updates + SharedPreferences for app startup
- **Proper system locale handling**: `LocaleHelper` manages locale configuration across Android versions
- **Resource localization**: All UI strings externalized to `strings.xml` files

### State Management
- Zoom level and pan position persistence
- Lock state maintained across rotations
- Configuration change handling via ViewModel
- Repository pattern for theme preferences with DataStore

## Architecture Patterns

### Repository Pattern Implementation
The app implements repository pattern for data persistence:
- **ThemeRepository**: Uses DataStore for theme preference persistence
- **LanguageRepository**: Dual storage approach with DataStore + SharedPreferences for language preferences
- **Centralized state access**: ViewModels interact with repositories, not direct storage
- **Reactive updates**: Repositories expose `Flow` for automatic UI updates

### ViewModel Factory Pattern
Custom ViewModel factories handle dependency injection:
- **SettingsViewModelFactory**: Injects both ThemeRepository and LanguageRepository into SettingsViewModel
- **ThemeViewModelFactory**: Legacy factory for ThemeViewModel (still used in some contexts)
- **Proper lifecycle management**: Ensures ViewModels receive required dependencies
- **Testability**: Facilitates dependency injection for testing

### Device Integration Patterns
**Broadcast Receiver Integration**:
- `ACTION_USER_PRESENT`: Detects device unlock events
- `ACTION_SCREEN_OFF`: Tracks device lock state
- **KeyguardManager**: Checks device lock status on app resume

**System Integration Flow**:
1. Device locks → `ACTION_SCREEN_OFF` → Set `wasDeviceLocked = true`
2. Device unlocks → `ACTION_USER_PRESENT` → Call `viewModel.unlock(isAutomatic = true)`
3. App resume with unlocked device → KeyguardManager check → Automatic unlock

### Toast Message System
**Context-aware messaging**:
- **Lock messages**: Include instructional tips about device unlock
- **Unlock messages**: Differentiate manual vs automatic unlock
- **Smart duration**: Longer display time for instructional content
- **Positioning**: Bottom-centered with collision avoidance

## Development Notes

### Modern Android Practices
- **Jetpack Compose**: Fully declarative UI with no XML layouts
- **Material3**: Latest Material Design with dynamic theming
- **StateFlow**: Reactive state management with automatic UI updates
- **DataStore**: Type-safe preference storage replacing SharedPreferences

### Permissions Configuration
Permissions are properly configured in `AndroidManifest.xml`:
- `READ_EXTERNAL_STORAGE` (for Android 12 and below)
- `READ_MEDIA_IMAGES` (for Android 13+)

### State Flow Architecture
The app uses modern reactive state management:
- `ImageViewerViewModel.state` exposes `StateFlow<ImageViewerState>`
- `SettingsViewModel.themePreference` and `SettingsViewModel.languagePreference` expose `StateFlow` for settings
- `ThemeViewModel.themePreference` exposes `StateFlow<ThemePreference>` (legacy, still used)
- UI automatically recomposes when state changes
- Unidirectional data flow with clear action methods

### Internationalization Architecture
**Immediate Language Switching**:
- `LocaleProvider` composable wraps UI content with localized context
- Uses `remember(languagePreference)` to create new context when language changes
- `CompositionLocalProvider` supplies localized context to all child components
- No app restart required - changes apply instantly through recomposition

**Dual Storage Strategy**:
- **DataStore**: Primary storage for reactive UI updates via StateFlow
- **SharedPreferences**: Secondary storage for immediate access during `Application.attachBaseContext()`
- `LanguageRepository.setLanguagePreference()` saves to both storage mechanisms

**System Language Reset**:
- `LocaleHelper.resetToSystemLocale()` properly resets from custom locales to system default
- Uses `Resources.getSystem()` to get actual device system locale
- Handles Android 13+ `LocaleManager` and legacy configuration approaches