package com.zac15987.lockview.data

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.max
import kotlin.math.min

@Stable
class ImageViewerState(
    imageWidth: Float = 0f,
    imageHeight: Float = 0f,
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero
) {
    // Animatable properties for smooth transitions
    private val _scale = Animatable(initialScale)
    private val _offset = Animatable(initialOffset, Offset.VectorConverter)
    
    // Image dimensions
    var imageWidth by mutableFloatStateOf(imageWidth)
        private set
    var imageHeight by mutableFloatStateOf(imageHeight)
        private set
        
    // Layout size
    var layoutSize by mutableStateOf(IntSize.Zero)
        internal set
    
    // Current transform values
    val scale: Float get() = _scale.value
    val offset: Offset get() = _offset.value
    
    // Existing app-specific properties
    var imageUri: Uri? by mutableStateOf(null)
    var isLocked: Boolean by mutableStateOf(false)
    var isLoading: Boolean by mutableStateOf(false)
    var error: String? by mutableStateOf(null)
    var toastMessage: String? by mutableStateOf(null)
    
    // Computed properties
    val imageAspectRatio: Float
        get() = if (imageHeight > 0) imageWidth / imageHeight else 1f
    
    val layoutAspectRatio: Float
        get() = if (layoutSize.height > 0) layoutSize.width.toFloat() / layoutSize.height else 1f
    
    val minScale: Float
        get() = if (layoutSize.width > 0 && layoutSize.height > 0) {
            val scaleX = layoutSize.width / imageWidth
            val scaleY = layoutSize.height / imageHeight
            min(scaleX, scaleY)
        } else 0.5f
    
    val maxScale: Float get() = max(minScale * 8f, 5f)
    
    // Animation functions
    suspend fun animateToStandard() = coroutineScope {
        val targetScale = minScale
        val targetOffset = Offset.Zero
        
        async {
            _scale.animateTo(targetScale, spring())
        }
        async {
            _offset.animateTo(targetOffset, spring())
        }
    }
    
    suspend fun animateToBig(center: Offset) = coroutineScope {
        val targetScale = min(maxScale, minScale * 2f)
        val bounds = calculateBounds(targetScale)
        val targetOffset = bounds.coerceIn(-center * (targetScale - 1f))
        
        async {
            _scale.animateTo(targetScale, spring())
        }
        async {
            _offset.animateTo(targetOffset, spring())
        }
    }
    
    suspend fun animateScale(targetScale: Float) {
        val constrainedScale = targetScale.coerceIn(minScale, maxScale)
        _scale.animateTo(constrainedScale, spring())
    }
    
    suspend fun animateOffset(targetOffset: Offset) {
        val bounds = calculateBounds(scale)
        val constrainedOffset = bounds.coerceIn(targetOffset)
        _offset.animateTo(constrainedOffset, spring())
    }
    
    // Immediate updates for gesture handling
    suspend fun updateScale(newScale: Float) {
        val constrainedScale = newScale.coerceIn(minScale, maxScale)
        _scale.snapTo(constrainedScale)
    }
    
    suspend fun updateOffset(newOffset: Offset) {
        val bounds = calculateBounds(scale)
        val constrainedOffset = bounds.coerceIn(newOffset)
        _offset.snapTo(constrainedOffset)
    }
    
    // Drag functionality
    suspend fun drag(dragAmount: Offset) {
        val newOffset = offset + dragAmount
        updateOffset(newOffset)
    }
    
    // Update image dimensions
    fun setImageSize(width: Float, height: Float) {
        imageWidth = width
        imageHeight = height
    }
    
    // Calculate bounds for current scale
    private fun calculateBounds(currentScale: Float): Bounds {
        if (layoutSize.width == 0 || layoutSize.height == 0) {
            return Bounds.EMPTY
        }
        
        val scaledImageWidth = imageWidth * currentScale
        val scaledImageHeight = imageHeight * currentScale
        
        val maxOffsetX = max(0f, (scaledImageWidth - layoutSize.width) / 2f)
        val maxOffsetY = max(0f, (scaledImageHeight - layoutSize.height) / 2f)
        
        return Bounds(
            left = -maxOffsetX,
            top = -maxOffsetY,
            right = maxOffsetX,
            bottom = maxOffsetY
        )
    }
}

// Legacy data class for simple state management
data class SimpleImageViewerState(
    val imageUri: Uri? = null,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isLocked: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val toastMessage: String? = null
)