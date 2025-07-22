package com.zac15987.lockview

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zac15987.lockview.ui.screens.ImageViewerScreen
import com.zac15987.lockview.ui.theme.LockViewTheme
import com.zac15987.lockview.viewmodel.ImageViewerViewModel

class MainActivity : ComponentActivity() {
    
    private var viewModel: ImageViewerViewModel? = null
    private var wasDeviceLocked = false
    private val keyguardManager by lazy { 
        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager 
    }
    
    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    // Device was unlocked by user
                    viewModel?.unlock()
                }
                Intent.ACTION_SCREEN_OFF -> {
                    wasDeviceLocked = true
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Register broadcast receiver for screen unlock events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT) // Device unlocked
            addAction(Intent.ACTION_SCREEN_OFF)   // Device locked
        }
        registerReceiver(screenUnlockReceiver, filter)
        
        setContent {
            LockViewTheme {
                val vm = viewModel<ImageViewerViewModel>()
                viewModel = vm // Store reference for unlock detection
                ImageViewerScreen(vm)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check if coming back from device lock screen
        if (wasDeviceLocked && !keyguardManager.isKeyguardLocked) {
            // Device was locked but now is unlocked - unlock the image too
            viewModel?.unlock()
            wasDeviceLocked = false
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // Check if device is being locked
        if (keyguardManager.isKeyguardLocked) {
            wasDeviceLocked = true
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenUnlockReceiver)
    }
}