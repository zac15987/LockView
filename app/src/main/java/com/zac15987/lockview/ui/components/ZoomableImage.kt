package com.zac15987.lockview.ui.components

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    var lastTapTime by remember { mutableStateOf(0L) }
    var tapCount by remember { mutableStateOf(0) }
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
            contentDescription = "Zoomable image",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .pointerInput(isLocked, scale) {
                    if (!isLocked) {
                        detectTransformGestures(
                            panZoomLock = false
                        ) { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(0.5f, 5f)
                            onScaleChange(newScale)
                            
                            val maxX = (size.width * (newScale - 1)) / 2
                            val maxY = (size.height * (newScale - 1)) / 2
                            
                            val newOffset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
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