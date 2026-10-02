package com.zac15987.lockview.ui.screens

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.zac15987.lockview.R
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zac15987.lockview.data.DonationOption
import com.zac15987.lockview.data.language.LanguagePreference
import com.zac15987.lockview.data.lockedcontrols.LockedControlsPreference
import com.zac15987.lockview.data.panrange.PanRangeRepository
import com.zac15987.lockview.data.puremode.PureModePreference
import com.zac15987.lockview.data.theme.ThemePreference
import com.zac15987.lockview.ui.components.AboutDialog
import com.zac15987.lockview.ui.components.ImageViewer
import com.zac15987.lockview.ui.components.LicensesDialog
import com.zac15987.lockview.MainActivity
import com.zac15987.lockview.viewmodel.ImageViewerViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

private fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun ImageViewerScreen(
    viewModel: ImageViewerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLockSettingsDialog by remember { mutableStateOf(false) }
    var showPanRangeDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    
    // Handle system bars visibility based on lock state
    LaunchedEffect(state.areSystemBarsHidden) {
        val activity = context.findActivity() as? MainActivity
        activity?.let {
            if (state.areSystemBarsHidden) {
                it.hideSystemBars()
            } else {
                it.showSystemBars()
            }
        }
    }
    
    val failedToLoadImageText = stringResource(R.string.failed_to_load_image)
    val imageLockedMessage = stringResource(R.string.image_locked_message)
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
        }
    }

    val lockedControlsPreference by settingsViewModel.lockedControlsPreference.collectAsStateWithLifecycle()
    val lockedControlsEnabled = lockedControlsPreference == LockedControlsPreference.ENABLED
    // Whether transform gestures (zoom/pan/rotate) can actually be operated right now.
    // Mirrors the condition used inside ImageViewer's gesture detectors.
    val gesturesAllowed = !state.isLocked || lockedControlsEnabled

    val panMinVisiblePercent by settingsViewModel.panMinVisiblePercent.collectAsStateWithLifecycle()

    val pureModePreference by settingsViewModel.pureModePreference.collectAsStateWithLifecycle()
    // In pure mode, hide every UI element (buttons + lock indicator) while the image is locked
    val hideAllUi = state.isLocked && pureModePreference == PureModePreference.ENABLED

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Image display
        if (state.imageUri != null) {
            ImageViewer(
                state = state,
                imageUri = state.imageUri,
                onLoading = { viewModel.setLoading(true) },
                onSuccess = { imageSize ->
                    viewModel.setImageSize(imageSize.width.toFloat(), imageSize.height.toFloat())
                    viewModel.setLoading(false)
                },
                onError = { viewModel.setError(failedToLoadImageText) },
                lockedControlsEnabled = lockedControlsEnabled,
                minVisibleFraction = panMinVisiblePercent / 100f
            )
        } else {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_image_selected),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.tap_to_select_image),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Subtle lock indicator in top-right corner
        if (state.isLocked && state.imageUri != null && !hideAllUi) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 32.dp, start = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.image_is_locked),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        
        // Loading indicator
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        state.error?.let { error ->
            Snackbar(
                // Sits above the FAB row (nav bar + 16dp padding + 56dp FAB) so the
                // dismiss action stays clear of the select-image button
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 88.dp, start = 16.dp, end = 16.dp),
                action = {
                    TextButton(onClick = { viewModel.setError(null) }) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                Text(error)
            }
        }
        
        // Toast message for lock/unlock feedback
        state.toastMessage?.let { message ->
            LaunchedEffect(message) {
                // Show longer duration for messages with tips (containing newlines)
                val duration = if (message.contains('\n')) 4000L else 2000L
                kotlinx.coroutines.delay(duration)
                viewModel.clearToast()
            }
            
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        // Stack above the error Snackbar when both are visible
                        bottom = if (state.error != null) 160.dp else 80.dp,
                        start = 24.dp,
                        end = 24.dp
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // FAB for image selection
        if (!hideAllUi) {
            FloatingActionButton(
                onClick = {
                    if (!state.isLocked) {
                        imagePicker.launch(arrayOf("image/*"))
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(16.dp),
                containerColor = if (state.isLocked)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.select_image),
                    tint = if (state.isLocked)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Lock button (only when unlocked)
        if (state.imageUri != null && !state.isLocked) {
            FloatingActionButton(
                onClick = { viewModel.lock(imageLockedMessage) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.lock_image)
                )
            }
        }

        // Rotation toggle button
        if (state.imageUri != null && !hideAllUi) {
            val rotationEnabledMsg = stringResource(R.string.rotation_enabled_message)
            val rotationDisabledMsg = stringResource(R.string.rotation_disabled_message)
            val rotationLockedHint = stringResource(R.string.rotation_locked_hint)

            // When gestures are blocked while locked, the toggle can't do anything.
            // Grey it out and, on tap, point the user to Lock Settings instead of
            // silently toggling a mode that won't respond.
            FloatingActionButton(
                onClick = {
                    if (gesturesAllowed) {
                        viewModel.toggleRotationMode(rotationEnabledMsg, rotationDisabledMsg)
                    } else {
                        viewModel.showToast(rotationLockedHint)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp),
                containerColor = if (state.isRotationEnabled && gesturesAllowed)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                val rotationActive = state.isRotationEnabled && gesturesAllowed
                Icon(
                    imageVector = if (rotationActive)
                        Icons.Default.Refresh
                    else
                        Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.toggle_rotation),
                    tint = if (rotationActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else if (gesturesAllowed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
        }

        // Menu button (top-right corner)
        if (!hideAllUi) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.menu),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.theme)) },
                        onClick = {
                            showMenu = false
                            showThemeDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.language)) },
                        onClick = {
                            showMenu = false
                            showLanguageDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.lock_settings)) },
                        onClick = {
                            showMenu = false
                            showLockSettingsDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.pan_range)) },
                        onClick = {
                            showMenu = false
                            showPanRangeDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.donate)) },
                        onClick = {
                            showMenu = false
                            showDonationDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.about)) },
                        onClick = {
                            showMenu = false
                            showAboutDialog = true
                        }
                    )
                }
            }
        }
    }
    
    // Dialogs
    if (showAboutDialog) {
        AboutDialog(
            onDismissRequest = { showAboutDialog = false },
            onShowLicenses = {
                showAboutDialog = false
                showLicensesDialog = true
            }
        )
    }
    
    if (showLicensesDialog) {
        LicensesDialog(
            onDismissRequest = { showLicensesDialog = false }
        )
    }
    
    // Theme selection dialog
    if (showThemeDialog) {
        val currentTheme by settingsViewModel.themePreference.collectAsStateWithLifecycle()
        
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.choose_theme)) },
            text = {
                Column {
                    ThemePreference.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setThemePreference(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = {
                                    settingsViewModel.setThemePreference(theme)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(theme.displayNameResId))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Language selection dialog
    if (showLanguageDialog) {
        val currentLanguage by settingsViewModel.languagePreference.collectAsStateWithLifecycle()
        
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.choose_language)) },
            text = {
                Column {
                    LanguagePreference.values().forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.setLanguagePreference(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentLanguage == language,
                                onClick = {
                                    settingsViewModel.setLanguagePreference(language)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(language.displayNameResId))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Lock settings dialog (gestures-when-locked + pure viewing mode)
    if (showLockSettingsDialog) {
        val currentLockedControls by settingsViewModel.lockedControlsPreference.collectAsStateWithLifecycle()
        val currentPureMode by settingsViewModel.pureModePreference.collectAsStateWithLifecycle()

        AlertDialog(
            onDismissRequest = { showLockSettingsDialog = false },
            title = { Text(stringResource(R.string.lock_settings)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Section 1: gestures when locked
                    Text(
                        text = stringResource(R.string.lock_gestures_section),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.locked_controls_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    PreferenceRadioGroup(
                        options = LockedControlsPreference.values().toList(),
                        isSelected = { it == currentLockedControls },
                        displayNameResId = { it.displayNameResId },
                        onSelect = { settingsViewModel.setLockedControlsPreference(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Section 2: pure viewing mode
                    Text(
                        text = stringResource(R.string.pure_mode_section),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = stringResource(R.string.pure_mode_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    PreferenceRadioGroup(
                        options = PureModePreference.values().toList(),
                        isSelected = { it == currentPureMode },
                        displayNameResId = { it.displayNameResId },
                        onSelect = { settingsViewModel.setPureModePreference(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // How to lock / unlock
                    Text(
                        text = stringResource(R.string.lock_usage_explanation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLockSettingsDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // Pan range dialog: how much of the image panning must leave on screen
    if (showPanRangeDialog) {
        // Local while dragging; saved once the slider is released
        var sliderPercent by remember { mutableFloatStateOf(panMinVisiblePercent.toFloat()) }
        val min = PanRangeRepository.MIN_PERCENT
        val max = PanRangeRepository.MAX_PERCENT

        AlertDialog(
            onDismissRequest = { showPanRangeDialog = false },
            title = { Text(stringResource(R.string.pan_range)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.pan_range_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.pan_range_value, sliderPercent.roundToInt()),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Slider(
                        value = sliderPercent,
                        onValueChange = { sliderPercent = it },
                        onValueChangeFinished = {
                            settingsViewModel.setPanMinVisiblePercent(sliderPercent.roundToInt())
                        },
                        valueRange = min.toFloat()..max.toFloat(),
                        // Intermediate stops between min and max, one per STEP_PERCENT
                        steps = (max - min) / PanRangeRepository.STEP_PERCENT - 1
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPanRangeDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    // Donation selection dialog
    if (showDonationDialog) {
        AlertDialog(
            onDismissRequest = { showDonationDialog = false },
            title = { Text(stringResource(R.string.choose_donation_method)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DonationOption.all().forEach { option ->
                        OutlinedButton(
                            onClick = {
                                showDonationDialog = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(option.url))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    viewModel.setError("No browser available")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(option.displayNameResId))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDonationDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun <T> PreferenceRadioGroup(
    options: List<T>,
    isSelected: (T) -> Boolean,
    displayNameResId: (T) -> Int,
    onSelect: (T) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(option) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected(option),
                    onClick = { onSelect(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(displayNameResId(option)))
            }
        }
    }
}