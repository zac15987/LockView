package com.zac15987.lockview

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zac15987.lockview.data.ImageViewerState
import com.zac15987.lockview.ui.components.ImageViewer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Drives real multi-touch input through ImageViewer on a device and checks that the image
 * point under the fingers stays under the fingers.
 */
@RunWith(AndroidJUnit4::class)
class ImageViewerGestureTest {

    @get:Rule
    val rule = createComposeRule()

    private lateinit var state: ImageViewerState
    private val node get() = rule.onNodeWithTag("viewer")
    private val center get() = Offset(state.layoutSize.width / 2f, state.layoutSize.height / 2f)

    @Before
    fun setUp() {
        state = ImageViewerState()
        var image by mutableStateOf<Any?>(null)
        rule.setContent {
            ImageViewer(
                state = state,
                imageUri = image,
                onLoading = {},
                onSuccess = {},
                onError = {},
                modifier = Modifier.testTag("viewer")
            )
        }
        rule.waitUntil(5_000) { state.layoutSize.width > 0 }
        // Same aspect ratio as the layout, so the image fills it on both axes and pan bounds
        // don't pin either axis; these tests are about the anchor, not edge clamping
        image = Bitmap.createBitmap(
            state.layoutSize.width, state.layoutSize.height, Bitmap.Config.ARGB_8888
        )
        rule.waitUntil(5_000) { state.imageWidth > 0f }
        rule.waitForIdle()
    }

    // Mirrors graphicsLayer: scale and rotate around the layout centre, then translate
    private fun toScreen(local: Offset): Offset {
        val r = Math.toRadians(state.rotation.toDouble())
        val d = (local - center) * state.scale
        return center + state.offset + Offset(
            (d.x * cos(r) - d.y * sin(r)).toFloat(),
            (d.x * sin(r) + d.y * cos(r)).toFloat()
        )
    }

    private fun toLocal(screen: Offset): Offset {
        val r = Math.toRadians(-state.rotation.toDouble())
        val d = screen - center - state.offset
        return center + Offset(
            (d.x * cos(r) - d.y * sin(r)).toFloat(),
            (d.x * sin(r) + d.y * cos(r)).toFloat()
        ) / state.scale
    }

    // On-screen size of the image at scale 1 (ContentScale.Fit)
    private fun contentSize(): Offset {
        val fit = min(
            state.layoutSize.width / state.imageWidth,
            state.layoutSize.height / state.imageHeight
        )
        return Offset(state.imageWidth * fit, state.imageHeight * fit)
    }

    private fun assertNear(expected: Offset, actual: Offset, tolerance: Float = 3f) {
        assertEquals("x", expected.x, actual.x, tolerance)
        assertEquals("y", expected.y, actual.y, tolerance)
    }

    /** Two fingers placed symmetrically around [centroid], [halfSpan] from it, at [angleDeg]. */
    private fun TouchInjectionScope.place(centroid: Offset, halfSpan: Float, angleDeg: Float) {
        val r = Math.toRadians(angleDeg.toDouble())
        val v = Offset((cos(r) * halfSpan).toFloat(), (sin(r) * halfSpan).toFloat())
        updatePointerTo(0, centroid - v)
        updatePointerTo(1, centroid + v)
        move()
    }

    private fun startTwoFingers(centroid: Offset, halfSpan: Float) {
        node.performTouchInput {
            down(0, centroid - Offset(halfSpan, 0f))
            down(1, centroid + Offset(halfSpan, 0f))
        }
    }

    /** Pinch around a fixed point: get past touch slop, then check the anchor while zooming. */
    private fun pinchAndCheck(at: Offset, fromSpan: Float, toSpan: Float) {
        startTwoFingers(at, fromSpan)
        node.performTouchInput { repeat(5) { i -> place(at, fromSpan + (i + 1) * 8f, 0f) } }
        rule.waitForIdle()
        val anchor = toLocal(at)
        val scaleBefore = state.scale
        val startSpan = fromSpan + 40f
        node.performTouchInput {
            val steps = 20
            repeat(steps) { i -> place(at, startSpan + (toSpan - startSpan) * (i + 1) / steps, 0f) }
        }
        rule.waitForIdle()
        assertTrue("scale should grow (${state.scale} vs $scaleBefore)", state.scale > scaleBefore * 1.15f)
        assertNear(at, toScreen(anchor))
        node.performTouchInput { up(0); up(1) }
        rule.waitForIdle()
    }

    @Test
    fun pinchNearEdge_pointStaysUnderFingers_alsoWhenAlreadyZoomed() {
        val c = contentSize()
        // Near the right/bottom edge of the visible image
        val at = center + Offset(c.x * 0.35f, c.y * 0.3f)

        pinchAndCheck(at, fromSpan = 60f, toSpan = 120f)
        // Second pinch at the same spot while already zoomed: the case that used to drift
        pinchAndCheck(at, fromSpan = 60f, toSpan = 130f)
    }

    @Test
    fun pinchWhileMoving_imageFollowsFingers() {
        val start = center
        startTwoFingers(start, 80f)
        node.performTouchInput { repeat(5) { i -> place(start, 80f + (i + 1) * 8f, 0f) } }
        rule.waitForIdle()
        val anchor = toLocal(start)

        val pan = Offset(-200f, -120f)
        node.performTouchInput {
            val steps = 25
            repeat(steps) { i ->
                val t = (i + 1f) / steps
                place(start + pan * t, 120f + 120f * t, 0f)
            }
        }
        rule.waitForIdle()
        assertTrue("scale ${state.scale}", state.scale > 1.5f)
        assertNear(start + pan, toScreen(anchor))
        node.performTouchInput { up(0); up(1) }
    }

    @Test
    fun oneFingerPanAfterZoom_followsFinger() {
        pinchAndCheck(center, fromSpan = 80f, toSpan = 260f)
        val from = center + Offset(100f, 0f)
        node.performTouchInput {
            down(0, from)
            repeat(5) { i -> moveTo(0, from - Offset((i + 1) * 6f, 0f)) }
        }
        rule.waitForIdle()
        val grabbed = from - Offset(30f, 0f)
        val anchor = toLocal(grabbed)
        node.performTouchInput {
            repeat(20) { i -> moveTo(0, grabbed - Offset((i + 1) * 10f, (i + 1) * 3f)) }
        }
        rule.waitForIdle()
        assertNear(grabbed - Offset(200f, 60f), toScreen(anchor))
        node.performTouchInput { up(0) }
    }

    @Test
    fun zoomedOutBelowScreenSize_oneFingerPanStillMoves() {
        // Pinch in to ~0.6x
        startTwoFingers(center, 300f)
        node.performTouchInput { repeat(25) { i -> place(center, 300f - (i + 1) * 5f, 0f) } }
        node.performTouchInput { up(0); up(1) }
        rule.waitForIdle()
        assertTrue("scale ${state.scale}", state.scale < 0.8f)

        val from = center
        node.performTouchInput {
            down(0, from)
            repeat(5) { i -> moveTo(0, from + Offset((i + 1) * 6f, 0f)) }
        }
        rule.waitForIdle()
        val grabbed = from + Offset(30f, 0f)
        val anchor = toLocal(grabbed)
        node.performTouchInput {
            repeat(20) { i -> moveTo(0, grabbed + Offset((i + 1) * 5f, (i + 1) * 8f)) }
        }
        rule.waitForIdle()
        assertNear(grabbed + Offset(100f, 160f), toScreen(anchor))
        node.performTouchInput { up(0) }
    }

    @Test
    fun rotateWithRotationEnabled_pointStaysUnderFingers() {
        state.isRotationEnabled = true
        rule.waitForIdle()
        val at = center + Offset(-60f, 40f)
        startTwoFingers(at, 150f)
        node.performTouchInput { repeat(5) { i -> place(at, 150f + (i + 1) * 8f, (i + 1) * 3f) } }
        rule.waitForIdle()
        val anchor = toLocal(at)
        val rotationBefore = state.rotation
        node.performTouchInput { repeat(15) { i -> place(at, 190f + (i + 1) * 6f, 15f + (i + 1) * 3f) } }
        rule.waitForIdle()
        assertEquals(rotationBefore + 45f, state.rotation, 2f)
        assertNear(at, toScreen(anchor), tolerance = 5f)
        node.performTouchInput { up(0); up(1) }
    }

    @Test
    fun doubleTapNearEdge_zoomsIntoTappedPoint() {
        val c = contentSize()
        val at = center + Offset(c.x * 0.2f, c.y * 0.2f)
        val anchor = toLocal(at)
        node.performTouchInput { doubleClick(at) }
        rule.waitForIdle()
        assertEquals(2f, state.scale, 0.01f)
        assertNear(at, toScreen(anchor))
    }

    @Test
    fun rotationDisabled_twoFingerTwistDoesNotRotate() {
        val at = center
        startTwoFingers(at, 150f)
        node.performTouchInput { repeat(20) { i -> place(at, 150f + i * 4f, (i + 1) * 3f) } }
        rule.waitForIdle()
        assertEquals(0f, state.rotation, 0.001f)
        node.performTouchInput { up(0); up(1) }
    }
}
