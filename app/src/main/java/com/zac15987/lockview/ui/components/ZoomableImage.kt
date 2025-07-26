package com.zac15987.lockview.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.zac15987.lockview.R
import com.zac15987.lockview.data.ImageViewerState
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun ImageViewer(
    state: ImageViewerState,
    imageUri: Any?,
    onLoading: () -> Unit,
    onSuccess: (IntSize) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.zoomable_image),
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    state.layoutSize = coordinates.size
                }
                .graphicsLayer {
                    scaleX = state.scale
                    scaleY = state.scale
                    translationX = state.offset.x
                    translationY = state.offset.y
                }
                .pointerInput(state.isLocked) {
                    if (!state.isLocked) {
                        detectZoom { centroid, zoom ->
                            coroutineScope.launch {
                                val newScale = (state.scale * zoom).coerceIn(state.minScale, state.maxScale)
                                
                                // Calculate new offset to zoom towards centroid
                                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                                val centroidOffset = centroid - centerOffset
                                val scaleDelta = newScale - state.scale
                                val newOffset = state.offset - centroidOffset * scaleDelta / state.scale
                                
                                state.updateScale(newScale)
                                state.updateOffset(newOffset)
                            }
                        }
                    }
                }
                .pointerInput(state.isLocked) {
                    if (!state.isLocked) {
                        detectDragGestures { _, dragAmount ->
                            coroutineScope.launch {
                                state.drag(dragAmount)
                            }
                        }
                    }
                }
                .pointerInput(state.isLocked) {
                    if (!state.isLocked) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                coroutineScope.launch {
                                    if (abs(state.scale - state.minScale) < 0.1f) {
                                        // Zoom in to double tap location
                                        state.animateToBig(tapOffset)
                                    } else {
                                        // Zoom out to standard view
                                        state.animateToStandard()
                                    }
                                }
                            }
                        )
                    }
                },
            contentScale = ContentScale.Fit,
            onLoading = { onLoading() },
            onSuccess = { result ->
                val drawable = result.result.drawable
                val imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)
                state.setImageSize(imageSize.width.toFloat(), imageSize.height.toFloat())
                onSuccess(imageSize)
            },
            onError = { onError() }
        )
    }
}

// Backward compatibility function
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
    // Create a temporary state for backward compatibility
    val state = remember {
        ImageViewerState(initialScale = scale, initialOffset = offset)
    }
    
    // Update state when external values change
    LaunchedEffect(scale, offset, isLocked) {
        state.updateScale(scale)
        state.updateOffset(offset)
        state.isLocked = isLocked
    }
    
    // Monitor state changes and notify parent
    LaunchedEffect(state.scale) {
        if (state.scale != scale) {
            onScaleChange(state.scale)
        }
    }
    
    LaunchedEffect(state.offset) {
        if (state.offset != offset) {
            onOffsetChange(state.offset)
        }
    }
    
    ImageViewer(
        state = state,
        imageUri = imageUri,
        onLoading = onLoading,
        onSuccess = { onSuccess() },
        onError = onError,
        modifier = modifier
    )
}