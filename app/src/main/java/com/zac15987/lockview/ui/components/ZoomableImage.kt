package com.zac15987.lockview.ui.components

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.*

@Composable
fun ZoomableImage(
    imageUri: Any?,
    isLocked: Boolean,
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onDoubleTap: () -> Unit,
    onLoading: () -> Unit,
    onSuccess: () -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    val coroutineScope = rememberCoroutineScope()
    
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = with(density) { maxWidth.toPx() }
        val maxHeight = with(density) { maxHeight.toPx() }
        containerSize = IntSize(maxWidth.toInt(), maxHeight.toInt())
        
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = "Zoomable image",
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    containerSize = coordinates.size
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(isLocked, scale, offset) {
                    if (!isLocked) {
                        detectTransformGestures(
                            panZoomLock = false
                        ) { centroid, pan, zoom, _ ->
                            // Improved zoom sensitivity with exponential scaling
                            val zoomFactor = if (zoom > 1f) {
                                1f + (zoom - 1f) * 1.5f // Make zoom in more sensitive
                            } else {
                                1f - (1f - zoom) * 1.2f // Make zoom out more sensitive
                            }
                            
                            val newScale = (scale * zoomFactor).coerceIn(0.5f, 15f)
                            
                            // Calculate proper pan boundaries based on scaled image size
                            val scaledWidth = size.width * newScale
                            val scaledHeight = size.height * newScale
                            
                            val maxOffsetX = max(0f, (scaledWidth - size.width) / 2f)
                            val maxOffsetY = max(0f, (scaledHeight - size.height) / 2f)
                            
                            // Apply zoom-relative panning adjustment
                            val adjustedPan = pan * (1f + (newScale - 1f) * 0.3f)
                            
                            val newOffset = Offset(
                                x = (offset.x + adjustedPan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                y = (offset.y + adjustedPan.y).coerceIn(-maxOffsetY, maxOffsetY)
                            )
                            
                            onScaleChange(newScale)
                            onOffsetChange(newOffset)
                        }
                    }
                }
                .pointerInput(isLocked) {
                    if (!isLocked) {
                        detectTapGestures(
                            onDoubleTap = { onDoubleTap() }
                        )
                    }
                },
            contentScale = ContentScale.Fit,
            onLoading = { onLoading() },
            onSuccess = { onSuccess() },
            onError = { onError() }
        )
    }
}