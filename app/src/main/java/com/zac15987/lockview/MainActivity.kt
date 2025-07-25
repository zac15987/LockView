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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zac15987.lockview.data.language.LanguageRepository
import com.zac15987.lockview.data.theme.ThemeRepository
import com.zac15987.lockview.ui.screens.ImageViewerScreen
import com.zac15987.lockview.ui.theme.LockViewTheme
import com.zac15987.lockview.ui.theme.LocaleProvider
import com.zac15987.lockview.utils.LocaleHelper
import com.zac15987.lockview.viewmodel.ImageViewerViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {
    
    private var viewModel: ImageViewerViewModel? = null
    private var wasDeviceLocked = false
    private val keyguardManager by lazy { 
        getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager 
    }
    
    private lateinit var languageRepository: LanguageRepository
    
    override fun attachBaseContext(newBase: Context) {
        // Initialize the repository here to be used later in onCreate
        languageRepository = LanguageRepository(newBase)
        
        // The locale is already applied by the Application class
        super.attachBaseContext(newBase)
    }
    
    
    private val screenUnlockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    // Device was unlocked by user
                    context?.let {
                        viewModel?.unlock(it.getString(R.string.image_unlocked_message))
                    }
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
            val themeRepository = ThemeRepository(this@MainActivity)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(themeRepository, languageRepository)
            )
            val themePreference = settingsViewModel.themePreference.collectAsStateWithLifecycle()
            val languageChanged = settingsViewModel.languageChanged.collectAsStateWithLifecycle()
            
            // Handle language change acknowledgment
            if (languageChanged.value) {
                settingsViewModel.onLanguageChangeHandled()
            }
            
            LockViewTheme(themePreference = themePreference.value) {
                LocaleProvider(settingsViewModel = settingsViewModel) {
                    val vm = viewModel<ImageViewerViewModel>()
                    viewModel = vm // Store reference for unlock detection
                    ImageViewerScreen(vm, settingsViewModel)
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Check if coming back from device lock screen
        if (wasDeviceLocked && !keyguardManager.isKeyguardLocked) {
            // Device was locked but now is unlocked - unlock the image too
            viewModel?.unlock(getString(R.string.image_unlocked_message))
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