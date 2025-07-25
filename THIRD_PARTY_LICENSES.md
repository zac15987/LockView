# Third-Party Licenses

This app uses the following third-party libraries:

## ImageViewer for Jetpack Compose

Copyright (c) 2024 ZhangKe (0xZhangKe)

Repository: https://github.com/0xZhangKe/ImageViewer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

---

### Modifications

The following files contain code adapted from the ImageViewer library:
- `app/src/main/java/com/zac15987/lockview/ui/components/TransformGestureDetector.kt`
- `app/src/main/java/com/zac15987/lockview/data/Bounds.kt`
- Parts of `app/src/main/java/com/zac15987/lockview/data/ImageViewerState.kt`
- Parts of `app/src/main/java/com/zac15987/lockview/ui/components/ZoomableImage.kt` (now ImageViewer)

These files have been modified to integrate with the LockView app's lock functionality and UI requirements.

---

## Additional Libraries

This app also uses various Android Jetpack libraries and other dependencies which are licensed under the Apache License, Version 2.0:

- AndroidX Core
- AndroidX Lifecycle
- AndroidX Activity Compose
- Jetpack Compose
- Coil (Image Loading Library)
- Material3

For the complete list of dependencies and their licenses, please refer to the app's build.gradle file.