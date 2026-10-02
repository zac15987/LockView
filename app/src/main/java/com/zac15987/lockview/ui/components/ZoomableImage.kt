package com.zac15987.lockview.ui.components

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
    modifier: Modifier = Modifier,
    lockedControlsEnabled: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Gestures are detected on this untransformed Box rather than on the image itself, so all
    // pointer positions are screen coordinates. On the image (after graphicsLayer) they would be
    // in the transformed space, which shifts under the fingers as the transform changes.
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isLocked, lockedControlsEnabled) {
                if (!state.isLocked || lockedControlsEnabled) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        coroutineScope.launch {
                            // Apply rotation only if rotation mode enabled
                            val rotationDelta = if (state.isRotationEnabled) rotation else 0f
                            state.transform(centroid, pan, zoom, rotationDelta)
                        }
                    }
                }
            }
            .pointerInput(state.isLocked, lockedControlsEnabled) {
                if (!state.isLocked || lockedControlsEnabled) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            coroutineScope.launch {
                                if (abs(state.scale - state.fitScale) < 0.1f) {
                                    // Zoom in to double tap location
                                    state.animateToBig(tapOffset)
                                } else {
                                    // Zoom out to fit-to-screen
                                    state.animateToStandard()
                                }
                            }
                        }
                    )
                }
            },
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
                    rotationZ = state.rotation
                },
            // Fit does the fit-to-screen sizing; graphicsLayer's scale is relative to that
            // (see ImageViewerState.fitScale), so the two must not both apply a fit factor.
            contentScale = ContentScale.Fit,
            onLoading = { onLoading() },
            onSuccess = { result ->
                val drawable = result.result.drawable
                val imageSize = IntSize(drawable.intrinsicWidth, drawable.intrinsicHeight)
                coroutineScope.launch {
                    state.setImageSize(imageSize.width.toFloat(), imageSize.height.toFloat())
                }
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