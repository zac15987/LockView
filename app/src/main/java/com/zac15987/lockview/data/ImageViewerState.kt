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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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
    private val _rotation = Animatable(0f)
    
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
    val rotation: Float get() = _rotation.value
    
    // Existing app-specific properties
    var imageUri: Uri? by mutableStateOf(null)
    var isLocked: Boolean by mutableStateOf(false)
    var areSystemBarsHidden: Boolean by mutableStateOf(false)
    var isLoading: Boolean by mutableStateOf(false)
    var error: String? by mutableStateOf(null)
    var toastMessage: String? by mutableStateOf(null)
    var isRotationEnabled: Boolean by mutableStateOf(false)
    
    // Computed properties
    val imageAspectRatio: Float
        get() = if (imageHeight > 0) imageWidth / imageHeight else 1f
    
    val layoutAspectRatio: Float
        get() = if (layoutSize.height > 0) layoutSize.width.toFloat() / layoutSize.height else 1f
    
    // Factor ContentScale.Fit already applies to draw the image inside the layout.
    // Used only to derive the on-screen content size for pan bounds.
    private val fitFactor: Float
        get() = if (layoutSize.width > 0 && layoutSize.height > 0 && imageWidth > 0 && imageHeight > 0) {
            min(layoutSize.width / imageWidth, layoutSize.height / imageHeight)
        } else 1f

    // Size the image occupies on screen at scale 1f
    private val contentWidth: Float get() = imageWidth * fitFactor
    private val contentHeight: Float get() = imageHeight * fitFactor

    // Fit-to-screen scale. ImageViewer draws with ContentScale.Fit, which already fits the
    // image to the layout, so the graphicsLayer scale is relative and 1f *is* fit-to-screen.
    // Applying a pixel-based fit factor here too would scale the image twice.
    val fitScale: Float get() = 1f

    // Minimum scale allows zooming out to 50% of fit-to-screen
    val minScale: Float get() = fitScale * 0.5f

    val maxScale: Float get() = fitScale * 8f
    
    // Animation functions
    suspend fun animateToStandard() = coroutineScope {
        val targetScale = fitScale
        val targetOffset = Offset.Zero

        async {
            _scale.animateTo(targetScale, spring())
        }
        async {
            _offset.animateTo(targetOffset, spring())
        }
        async {
            _rotation.animateTo(0f, spring())
        }
    }
    
    suspend fun animateToBig(center: Offset) = coroutineScope {
        val targetScale = min(maxScale, fitScale * 2f)
        val bounds = calculateBounds(targetScale)

        // graphicsLayer scales around the layout centre, so the tap must be expressed relative
        // to it: keeping the tapped point still means offset = d * (1 - k) + offset * k, where
        // d is the tap's distance from the centre and k the scale ratio.
        val layoutCenter = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
        val d = center - layoutCenter
        val k = if (scale > 0f) targetScale / scale else targetScale
        val targetOffset = bounds.coerceIn(d * (1f - k) + offset * k)

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
    
    // Combined pinch/pan/rotate step. centroid and pan are in screen (layout) coordinates, not
    // the transformed image's. The image point under the previous centroid (centroid - pan) is
    // kept under the current centroid: with d = point - layout centre and k = scale ratio,
    // newOffset = dCurrent - R(rotationDelta) * k * (dPrevious - offset).
    suspend fun transform(centroid: Offset, pan: Offset, zoom: Float, rotationDelta: Float) {
        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
        val k = if (scale > 0f) newScale / scale else 1f

        val layoutCenter = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
        val current = centroid - layoutCenter
        val previous = current - pan
        val newOffset = current - (previous - offset).rotateBy(rotationDelta) * k

        _scale.snapTo(newScale)
        if (rotationDelta != 0f) {
            _rotation.snapTo((rotation + rotationDelta) % 360f)
        }
        updateOffset(newOffset)
    }

    private fun Offset.rotateBy(degrees: Float): Offset {
        if (degrees == 0f) return this
        val radians = Math.toRadians(degrees.toDouble())
        val cosR = cos(radians).toFloat()
        val sinR = sin(radians).toFloat()
        return Offset(x * cosR - y * sinR, x * sinR + y * cosR)
    }

    // Drag functionality - screen-relative panning with rotation compensation
    suspend fun drag(dragAmount: Offset) {
        val rotationRadians = Math.toRadians(rotation.toDouble())
        val cosR = cos(rotationRadians).toFloat()
        val sinR = sin(rotationRadians).toFloat()

        // Forward rotation to pre-compensate for graphicsLayer's rotation
        val transformedDrag = Offset(
            x = dragAmount.x * cosR - dragAmount.y * sinR,
            y = dragAmount.x * sinR + dragAmount.y * cosR
        )

        val scaleFactor = if (fitScale > 0f) scale / fitScale else 1f
        val newOffset = offset + transformedDrag * scaleFactor
        updateOffset(newOffset)
    }

    // Rotation functionality
    suspend fun updateRotation(newRotation: Float) {
        val normalized = newRotation % 360f
        _rotation.snapTo(normalized)
    }

    suspend fun animateRotation(targetRotation: Float) {
        _rotation.animateTo(targetRotation, spring())
    }

    suspend fun resetRotation() {
        _rotation.snapTo(0f)
    }

    // Update image dimensions and set initial scale to fit-to-screen
    suspend fun setImageSize(width: Float, height: Float) {
        imageWidth = width
        imageHeight = height
        // Set initial scale to fit-to-screen
        _scale.snapTo(fitScale)
    }
    
    // Calculate bounds for current scale
    private fun calculateBounds(currentScale: Float): Bounds {
        if (layoutSize.width == 0 || layoutSize.height == 0) {
            return Bounds.EMPTY
        }
        
        // Bounds are based on the size the image actually occupies on screen, which is the
        // ContentScale.Fit content size times the current (relative) scale
        val scaledImageWidth = contentWidth * currentScale
        val scaledImageHeight = contentHeight * currentScale
        
        // Per axis: larger than the layout -> the image edge can go at most to the layout edge;
        // smaller (zoomed out, or the letterboxed axis) -> it can move as long as it stays fully
        // inside the layout. Both are |scaled - layout| / 2.
        val maxOffsetX = abs(scaledImageWidth - layoutSize.width) / 2f
        val maxOffsetY = abs(scaledImageHeight - layoutSize.height) / 2f
        
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