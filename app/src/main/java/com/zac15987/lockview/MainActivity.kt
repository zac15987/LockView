package com.zac15987.lockview

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zac15987.lockview.data.language.LanguageRepository
import com.zac15987.lockview.data.lockedcontrols.LockedControlsRepository
import com.zac15987.lockview.data.puremode.PureModeRepository
import com.zac15987.lockview.data.theme.ThemeRepository
import com.zac15987.lockview.ui.screens.ImageViewerScreen
import com.zac15987.lockview.ui.theme.LockViewTheme
import com.zac15987.lockview.ui.theme.LocaleProvider
import com.zac15987.lockview.utils.LocaleHelper
import com.zac15987.lockview.utils.extractSharedImageUri
import com.zac15987.lockview.viewmodel.ImageViewerViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    
    private var viewModel: ImageViewerViewModel? = null
    private var wasDeviceLocked = false

    // Single-shot channel for images shared in from other apps. The ViewModel only
    // becomes available during composition, so the result is parked here until then.
    private val pendingSharedImage = MutableStateFlow<SharedImage?>(null)
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
                        showSystemBars()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    wasDeviceLocked = true
                }
            }
        }
    }
    
    fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    
    fun showSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        
        // Register broadcast receiver for screen unlock events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_USER_PRESENT) // Device unlocked
            addAction(Intent.ACTION_SCREEN_OFF)   // Device locked
        }
        registerReceiver(screenUnlockReceiver, filter)

        // Only on a fresh start: a configuration change re-runs onCreate with the same
        // intent, and re-handling it would reset the transform and drop the lock state
        if (savedInstanceState == null) {
            handleSharedIntent(intent)
        }

        setContent {
            val themeRepository = ThemeRepository(this@MainActivity)
            val lockedControlsRepository = LockedControlsRepository(this@MainActivity)
            val pureModeRepository = PureModeRepository(this@MainActivity)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(themeRepository, languageRepository, lockedControlsRepository, pureModeRepository)
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

                    val unlockedMessage = stringResource(R.string.image_unlocked_message)
                    val sharedImageErrorMessage = stringResource(R.string.failed_to_load_shared_image)
                    // Keyed on the messages too, so a language change doesn't leave stale text
                    LaunchedEffect(vm, unlockedMessage, sharedImageErrorMessage) {
                        pendingSharedImage.collect { shared ->
                            when (shared) {
                                null -> return@collect
                                is SharedImage.Ready -> {
                                    // An explicit share is intentional: release the lock
                                    if (vm.state.value.isLocked) {
                                        vm.unlock(unlockedMessage)
                                    }
                                    vm.setError(null)
                                    vm.setImageUri(shared.uri)
                                }
                                SharedImage.Failed -> vm.setError(sharedImageErrorMessage)
                            }
                            pendingSharedImage.value = null
                        }
                    }

                    ImageViewerScreen(vm, settingsViewModel)
                }
            }
        }
    }
    
    /** Result of parsing an incoming ACTION_SEND intent. */
    private sealed interface SharedImage {
        data class Ready(val uri: Uri) : SharedImage
        data object Failed : SharedImage
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSharedIntent(intent)
    }

    /**
     * Picks up an image shared in from another app. The result is handed to the ViewModel by
     * the collector inside [setContent], which also resets the transform and lock state.
     */
    private fun handleSharedIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_SEND) return

        val uri = intent.extractSharedImageUri()
        pendingSharedImage.value = if (uri != null) {
            SharedImage.Ready(uri)
        } else {
            SharedImage.Failed
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Check if coming back from device lock screen
        if (wasDeviceLocked && !keyguardManager.isKeyguardLocked) {
            // Device was locked but now is unlocked - unlock the image too
            viewModel?.unlock(getString(R.string.image_unlocked_message))
            showSystemBars()
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