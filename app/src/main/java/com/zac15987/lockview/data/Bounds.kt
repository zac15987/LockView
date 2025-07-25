package com.zac15987.lockview.data

import androidx.compose.ui.geometry.Offset
import kotlin.math.max
import kotlin.math.min

data class Bounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val isEmpty: Boolean
        get() = left >= right || top >= bottom

    fun inside(x: Float, y: Float): Boolean {
        return x >= left && x <= right && y >= top && y <= bottom
    }

    fun inside(offset: Offset): Boolean {
        return inside(offset.x, offset.y)
    }

    fun coerceInX(x: Float): Float {
        return when {
            isEmpty -> x
            x < left -> left
            x > right -> right
            else -> x
        }
    }

    fun coerceInY(y: Float): Float {
        return when {
            isEmpty -> y
            y < top -> top
            y > bottom -> bottom
            else -> y
        }
    }

    fun coerceIn(offset: Offset): Offset {
        return Offset(
            x = coerceInX(offset.x),
            y = coerceInY(offset.y)
        )
    }

    fun width(): Float = right - left

    fun height(): Float = bottom - top

    fun center(): Offset = Offset((left + right) / 2f, (top + bottom) / 2f)

    companion object {
        val EMPTY = Bounds(0f, 0f, 0f, 0f)
        
        fun fromSize(width: Float, height: Float): Bounds {
            return Bounds(0f, 0f, width, height)
        }
        
        fun centered(centerX: Float, centerY: Float, width: Float, height: Float): Bounds {
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            return Bounds(
                left = centerX - halfWidth,
                top = centerY - halfHeight,
                right = centerX + halfWidth,
                bottom = centerY + halfHeight
            )
        }
    }
}