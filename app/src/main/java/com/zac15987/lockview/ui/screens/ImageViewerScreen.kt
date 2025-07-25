package com.zac15987.lockview.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zac15987.lockview.ui.components.AboutDialog
import com.zac15987.lockview.ui.components.ImageViewer
import com.zac15987.lockview.ui.components.LicensesDialog
import com.zac15987.lockview.utils.ImagePermissionHandler
import com.zac15987.lockview.utils.hasImagePermission
import com.zac15987.lockview.viewmodel.ImageViewerViewModel

@Composable
fun ImageViewerScreen(
    viewModel: ImageViewerViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    
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
                onError = { viewModel.setError("Failed to load image") }
            )
        } else {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No image selected",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap the + button to select an image",
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
                    contentDescription = "Image is locked",
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
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
        
        // Toast message for lock/unlock feedback
        state.toastMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2000) // Show for 2 seconds
                viewModel.clearToast()
            }
            
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium
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
                contentDescription = "Select image",
                tint = if (state.isLocked)
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Lock button (only when unlocked)
        if (state.imageUri != null && !state.isLocked) {
            FloatingActionButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "Lock image"
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
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("About") },
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
    
    // Permission denied dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app needs permission to access your images. Please grant the permission in Settings.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}