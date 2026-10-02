package com.zac15987.lockview.data

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin

class ImageViewerStateTransformTest {

    private val layout = IntSize(1000, 2000)
    private val center = Offset(500f, 1000f)
    private lateinit var state: ImageViewerState

    @Before
    fun setUp() = runBlocking {
        state = ImageViewerState()
        state.layoutSize = layout
        // Same aspect ratio as the layout, so the image fills it at scale 1
        state.setImageSize(1000f, 2000f)
    }

    // Mirrors graphicsLayer: scale and rotate around the layout centre, then translate
    private fun toScreen(local: Offset): Offset {
        val r = Math.toRadians(state.rotation.toDouble())
        val d = (local - center) * state.scale
        val rotated = Offset(
            (d.x * cos(r) - d.y * sin(r)).toFloat(),
            (d.x * sin(r) + d.y * cos(r)).toFloat()
        )
        return center + state.offset + rotated
    }

    private fun toLocal(screen: Offset): Offset {
        val r = Math.toRadians(-state.rotation.toDouble())
        val d = screen - center - state.offset
        val unrotated = Offset(
            (d.x * cos(r) - d.y * sin(r)).toFloat(),
            (d.x * sin(r) + d.y * cos(r)).toFloat()
        )
        return center + unrotated / state.scale
    }

    private fun assertOffsetEquals(expected: Offset, actual: Offset) {
        assertEquals(expected.x, actual.x, 0.5f)
        assertEquals(expected.y, actual.y, 0.5f)
    }

    @Test
    fun pinchAtEdgeKeepsPointUnderFingers_evenWhenAlreadyZoomed() = runBlocking {
        val finger = Offset(900f, 1800f)
        state.transform(finger, Offset.Zero, 2f, 0f)
        val anchor = toLocal(finger)

        // Second pinch at the same spot while already at 2x (old code drifted here)
        state.transform(finger, Offset.Zero, 1.5f, 0f)

        assertEquals(3f, state.scale, 0.001f)
        assertOffsetEquals(finger, toScreen(anchor))
    }

    @Test
    fun pinchFollowsCentroidMovement() = runBlocking {
        state.transform(Offset(500f, 1000f), Offset.Zero, 3f, 0f)
        val previous = Offset(600f, 1200f)
        val anchor = toLocal(previous)
        val pan = Offset(-80f, -50f)

        state.transform(previous + pan, pan, 1.2f, 0f)

        assertOffsetEquals(previous + pan, toScreen(anchor))
    }

    @Test
    fun rotationKeepsPointUnderFingers() = runBlocking {
        state.transform(center, Offset.Zero, 3f, 0f)
        val finger = Offset(400f, 900f)
        val anchor = toLocal(finger)

        state.transform(finger, Offset.Zero, 1.1f, 30f)

        assertEquals(30f, state.rotation, 0.001f)
        assertOffsetEquals(finger, toScreen(anchor))
    }

    @Test
    fun zoomedInPanLeavesTenPercentOfTheImageOnScreen() = runBlocking {
        state.transform(center, Offset.Zero, 2f, 0f)

        state.transform(center + Offset(5000f, 0f), Offset(5000f, 0f), 1f, 0f)

        // Image is 2000px wide at 2x on a 1000px layout -> 10% of it (200px) stays on screen
        assertOffsetEquals(Offset(1300f, 0f), state.offset)
    }

    @Test
    fun zoomedOutImageCanMove() = runBlocking {
        state.transform(center, Offset.Zero, 0.6f, 0f)

        state.transform(center + Offset(-100f, 150f), Offset(-100f, 150f), 1f, 0f)
        assertOffsetEquals(Offset(-100f, 150f), state.offset)

        state.transform(center + Offset(5000f, -5000f), Offset(5000f, -5000f), 1f, 0f)
        // 600x1200 image on a 1000x2000 layout -> 10% of it (60px / 120px) stays on screen
        assertOffsetEquals(Offset(740f, -1480f), state.offset)
    }

    @Test
    fun atFitScale_panLeavesTenPercentOfTheImageOnScreen() = runBlocking {
        state.transform(center + Offset(-5000f, 5000f), Offset(-5000f, 5000f), 1f, 0f)

        // 1000x2000 image filling a 1000x2000 layout -> 90% of it can go off screen
        assertEquals(-900f, state.offset.x, 0.5f)
        assertEquals(1800f, state.offset.y, 0.5f)
    }

    @Test
    fun raisingMinVisibleFractionReclampsCurrentOffset() = runBlocking {
        state.transform(center + Offset(-5000f, 0f), Offset(-5000f, 0f), 1f, 0f)
        assertEquals(-900f, state.offset.x, 0.5f)

        state.updateMinVisibleFraction(0.5f)

        // Half of the 1000px image must now stay on screen
        assertEquals(-500f, state.offset.x, 0.5f)
    }
}
