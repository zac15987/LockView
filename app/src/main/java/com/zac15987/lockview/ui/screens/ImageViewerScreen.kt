package com.zac15987.lockview.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
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
import com.zac15987.lockview.ui.components.ZoomableImage
import com.zac15987.lockview.ui.gestures.detectUnlockGestures
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
            .detectUnlockGestures(
                isLocked = state.isLocked,
                onUnlock = viewModel::unlock
            )
    ) {
        // Image display
        if (state.imageUri != null) {
            ZoomableImage(
                imageUri = state.imageUri,
                isLocked = state.isLocked,
                scale = state.scale,
                offset = state.offset,
                onScaleChange = viewModel::updateScale,
                onOffsetChange = viewModel::updateOffset,
                onDoubleTap = {
                    if (state.scale == 1f) {
                        viewModel.updateScale(2f)
                    } else {
                        viewModel.resetTransform()
                    }
                },
                onLoading = { viewModel.setLoading(true) },
                onSuccess = { viewModel.setLoading(false) },
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
        
        // Lock indicator overlay
        if (state.isLocked) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Image Locked",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Triple tap, long press, or shake to unlock",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        
        // FAB for image selection
        FloatingActionButton(
            onClick = {
                if (hasImagePermission(context)) {
                    imagePicker.launch("image/*")
                } else {
                    showPermissionDeniedDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Select image")
        }
        
        // Lock/Unlock button
        if (state.imageUri != null) {
            FloatingActionButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = if (state.isLocked) Icons.Default.Lock else Icons.Outlined.Lock,
                    contentDescription = if (state.isLocked) "Unlock" else "Lock"
                )
            }
        }
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