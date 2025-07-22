package com.zac15987.lockview.ui.gestures

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun Modifier.detectUnlockGestures(
    isLocked: Boolean,
    onUnlock: () -> Unit
): Modifier {
    // No touch-based unlock mechanisms - only device screen unlock is supported
    return this
}

@Composable
fun rememberVolumeButtonUnlock(
    isLocked: Boolean,
    onUnlock: () -> Unit
) {
    // This would require platform-specific implementation
    // For now, we'll use a placeholder that could be expanded
    // In a real implementation, you'd need to override onKeyDown in Activity
}