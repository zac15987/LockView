package com.zac15987.lockview.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
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

/**
 * Pan, pinch-zoom and rotation in a single detector, so a pinch can move the image at the same
 * time (and a separate drag detector can't double-apply the same finger movement).
 * Works with one finger (pan only) or more. [centroid] is the current centroid and [pan] the
 * centroid movement since the previous event, both in this node's coordinates.
 */
suspend fun PointerInputScope.detectTransformGestures(
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Unit
) {
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        var zoom = 1f
        var pan = Offset.Zero
        var rotation = 0f
        var pastTouchSlop = false
        awaitFirstDown(requireUnconsumed = false)

        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.any { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop || rotationMotion > touchSlop || panMotion > touchSlop) {
                        pastTouchSlop = true
                    }
                }

                if (pastTouchSlop) {
                    // Base threshold of 1.0° filters angle noise from fingers that are only
                    // pinching or panning
                    val effectiveRotation = if (abs(rotationChange) >= 1.0f) rotationChange else 0f
                    if (zoomChange != 1f || effectiveRotation != 0f || panChange != Offset.Zero) {
                        val centroid = event.calculateCentroid(useCurrent = true)
                        onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }
                    event.changes.forEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
    }
}
