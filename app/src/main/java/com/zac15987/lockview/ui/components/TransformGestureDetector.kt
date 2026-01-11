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
import kotlin.math.atan2
import kotlin.math.PI

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

suspend fun PointerInputScope.detectRotation(
    onRotate: (centroid: Offset, rotation: Float) -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        var rotation = 0f
        var pastTouchSlop = false
        val down = awaitFirstDown(requireUnconsumed = false)
        var previousAngle = 0f

        do {
            val event = awaitPointerEvent()

            // Require exactly 2 fingers
            if (event.changes.size != 2) {
                return@awaitEachGesture
            }

            // Calculate angle between two pointers
            val (pointer1, pointer2) = event.changes
            val angle = calculateAngle(pointer1.position, pointer2.position)
            val centroid = event.calculateCentroid(useCurrent = false)

            if (pastTouchSlop) {
                val angleDelta = angle - previousAngle
                // Normalize to -180 to 180 range
                val normalizedDelta = ((angleDelta + 180) % 360) - 180

                if (normalizedDelta != 0f) {
                    onRotate(centroid, normalizedDelta)
                }
                event.changes.forEach {
                    if (it.positionChanged()) {
                        it.consume()
                    }
                }
            } else {
                rotation += angle - previousAngle
                if (abs(rotation) > touchSlop) {
                    pastTouchSlop = true
                }
            }

            previousAngle = angle
        } while (event.changes.any { it.pressed })
    }
}

private fun calculateAngle(p1: Offset, p2: Offset): Float {
    return atan2(p2.y - p1.y, p2.x - p1.x) * 180f / PI.toFloat()
}

