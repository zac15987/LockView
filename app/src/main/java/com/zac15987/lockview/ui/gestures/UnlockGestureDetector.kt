package com.zac15987.lockview.ui.gestures

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.math.sqrt

@Composable
fun Modifier.detectUnlockGestures(
    isLocked: Boolean,
    onUnlock: () -> Unit
): Modifier {
    val context = LocalContext.current
    var lastTapTime by remember { mutableStateOf(0L) }
    var tapCount by remember { mutableStateOf(0) }
    
    DisposableEffect(isLocked) {
        if (isLocked) {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
            var lastShakeTime = 0L
            var shakeCount = 0
            
            val shakeListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    
                    val acceleration = sqrt(x * x + y * y + z * z)
                    
                    if (acceleration > 15) { // Shake threshold
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastShakeTime > 500) {
                            shakeCount++
                            lastShakeTime = currentTime
                            
                            if (shakeCount >= 3) {
                                onUnlock()
                                shakeCount = 0
                            }
                        }
                    }
                }
                
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            
            accelerometer?.let {
                sensorManager.registerListener(
                    shakeListener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            
            onDispose {
                sensorManager.unregisterListener(shakeListener)
            }
        } else {
            onDispose { }
        }
    }
    
    return this.then(
        if (isLocked) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastTapTime < 500) {
                            tapCount++
                            if (tapCount >= 3) {
                                onUnlock()
                                tapCount = 0
                            }
                        } else {
                            tapCount = 1
                        }
                        lastTapTime = currentTime
                    },
                    onLongPress = {
                        onUnlock()
                    }
                )
            }
        } else {
            Modifier
        }
    )
}

@Composable
fun rememberVolumeButtonUnlock(
    isLocked: Boolean,
    onUnlock: () -> Unit
) {
    // This would require platform-specific implementation
    // For now, we'll use a placeholder that could be expanded
    // In a real implementation, you'd need to override onKeyDown in Activity
}