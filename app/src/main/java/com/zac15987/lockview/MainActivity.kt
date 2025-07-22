package com.zac15987.lockview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zac15987.lockview.ui.screens.ImageViewerScreen
import com.zac15987.lockview.ui.theme.LockViewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockViewTheme {
                ImageViewerScreen()
            }
        }
    }
}