package com.zac15987.lockview.data

import android.net.Uri
import androidx.compose.ui.geometry.Offset

data class ImageViewerState(
    val imageUri: Uri? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isLocked: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)