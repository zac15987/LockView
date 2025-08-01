package com.zac15987.lockview.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Lock
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
import com.zac15987.lockview.data.theme.ThemePreference
import com.zac15987.lockview.ui.components.AboutDialog
import com.zac15987.lockview.ui.components.ImageViewer
import com.zac15987.lockview.ui.components.LicensesDialog
import com.zac15987.lockview.utils.ImagePermissionHandler
import com.zac15987.lockview.utils.hasImagePermission
import com.zac15987.lockview.viewmodel.ImageViewerViewModel
import com.zac15987.lockview.viewmodel.SettingsViewModel

@Composable
fun ImageViewerScreen(
    viewModel: ImageViewerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    
    val failedToLoadImageText = stringResource(R.string.failed_to_load_image)
    val imageLockedMessage = stringResource(R.string.image_locked_message)
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
        }
    }
    
    if (!hasImagePermission(context)) {
        ImagePermissionHandler(
            onPermissionGranted = { },
            onPermissionDenied = { showPermissionDeniedDialog = true }
        )
    }
    
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
                onError = { viewModel.setError(failedToLoadImageText) }
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
        if (state.isLocked && state.imageUri != null) {
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
                modifier = Modifier.align(Alignment.BottomCenter),
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
                    .padding(
                        bottom = if (state.error != null) 120.dp else 80.dp,
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
        FloatingActionButton(
            onClick = {
                if (!state.isLocked) {
                    if (hasImagePermission(context)) {
                        imagePicker.launch("image/*")
                    } else {
                        showPermissionDeniedDialog = true
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
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
        
        // Lock button (only when unlocked)
        if (state.imageUri != null && !state.isLocked) {
            FloatingActionButton(
                onClick = { viewModel.lock(imageLockedMessage) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = stringResource(R.string.lock_image)
                )
            }
        }
        
        // Menu button (top-right corner)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 8.dp)
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
    
    // Permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.permission_message)) },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}