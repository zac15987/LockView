package com.zac15987.lockview.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.ViewConfiguration
import kotlin.math.abs

suspend fun PointerInputScope.detectZoom(
    onZoom: (centroid: Offset, zoom: Float) -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        var zoom = 1f
        var pastTouchSlop = false
        val down = awaitFirstDown(requireUnconsumed = false)
        var pointer = down
        var pointerId = down.id

        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.id == pointerId && it.pressed != down.pressed }
            if (canceled) {
                return@awaitEachGesture
            }

            if (event.changes.size < 2) {
                return@awaitEachGesture
            }

            val zoomChange = event.calculateZoom()
            val centroid = event.calculateCentroid(useCurrent = false)
            
            if (!pastTouchSlop) {
                zoom *= zoomChange
                val centroidChange = centroid - event.calculateCentroid(useCurrent = true)
                val zoomMotion = abs(1 - zoom) * centroid.getDistance()
                val centroidMotion = centroidChange.getDistance()
                
                if (zoomMotion > touchSlop || centroidMotion > touchSlop) {
                    pastTouchSlop = true
                }
            }

            if (pastTouchSlop) {
                if (zoomChange != 1f) {
                    onZoom(centroid, zoomChange)
                }
                event.changes.forEach {
                    if (it.positionChanged()) {
                        it.consume()
                    }
                }
            }
        } while (event.changes.any { it.pressed })
    }
}

