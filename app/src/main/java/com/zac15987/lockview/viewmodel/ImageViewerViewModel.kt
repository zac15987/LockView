package com.zac15987.lockview.viewmodel

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zac15987.lockview.R
import com.zac15987.lockview.data.ImageViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ImageViewerViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(ImageViewerState())
    val state: StateFlow<ImageViewerState> = _state.asStateFlow()
    
    fun setImageUri(uri: Uri?) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.imageUri = uri
                if (uri != null) {
                    // Reset transform when new image is loaded - use immediate update
                    currentState.updateScale(currentState.minScale)
                    currentState.updateOffset(Offset.Zero)
                }
                currentState
            }
        }
    }
    
    fun updateScale(scale: Float) {
        viewModelScope.launch {
            _state.value.updateScale(scale)
        }
    }
    
    fun updateOffset(offset: Offset) {
        viewModelScope.launch {
            _state.value.updateOffset(offset)
        }
    }

    fun toggleLock(lockedMessage: String, unlockedMessage: String) {
        _state.update { currentState ->
            val newLockState = !currentState.isLocked
            currentState.isLocked = newLockState
            currentState.areSystemBarsHidden = newLockState
            currentState.toastMessage = if (newLockState) {
                lockedMessage
            } else {
                unlockedMessage
            }
            currentState
        }
    }
    
    fun lock(lockedMessage: String) {
        _state.update { currentState ->
            currentState.isLocked = true
            currentState.areSystemBarsHidden = true
            currentState.toastMessage = lockedMessage
            currentState
        }
    }
    
    fun unlock(unlockedMessage: String) {
        _state.update { currentState ->
            currentState.isLocked = false
            currentState.areSystemBarsHidden = false
            currentState.toastMessage = unlockedMessage
            currentState
        }
    }
    
    fun setSystemBarsHidden(hidden: Boolean) {
        _state.update { currentState ->
            currentState.areSystemBarsHidden = hidden
            currentState
        }
    }
    
    fun clearToast() {
        _state.update { currentState ->
            currentState.toastMessage = null
            currentState
        }
    }
    
    fun resetTransform() {
        viewModelScope.launch {
            _state.value.updateScale(_state.value.minScale)
            _state.value.updateOffset(Offset.Zero)
        }
    }
    
    fun setLoading(isLoading: Boolean) {
        _state.update { currentState ->
            currentState.isLoading = isLoading
            currentState
        }
    }
    
    fun setError(error: String?) {
        _state.update { currentState ->
            currentState.error = error
            currentState.isLoading = false
            currentState
        }
    }
    
    fun setImageSize(width: Float, height: Float) {
        viewModelScope.launch {
            _state.value.setImageSize(width, height)
            // After setting image size, scale to fit screen
            _state.value.updateScale(_state.value.minScale)
            _state.value.updateOffset(Offset.Zero)
        }
    }
    
    // Drag functionality
    fun drag(dragAmount: Offset) {
        viewModelScope.launch {
            _state.value.drag(dragAmount)
        }
    }
}