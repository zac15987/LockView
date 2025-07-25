package com.zac15987.lockview.viewmodel

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zac15987.lockview.data.ImageViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewerViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(ImageViewerState())
    val state: StateFlow<ImageViewerState> = _state.asStateFlow()
    
    fun setImageUri(uri: Uri?) {
        viewModelScope.launch {
            _state.value.imageUri = uri
            if (uri != null) {
                // Reset transform when new image is loaded - use immediate update
                _state.value.updateScale(_state.value.minScale)
                _state.value.updateOffset(Offset.Zero)
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
    
    
    fun toggleLock() {
        val newLockState = !_state.value.isLocked
        _state.value.isLocked = newLockState
        _state.value.toastMessage = if (newLockState) "Image locked" else "Image unlocked"
    }
    
    fun unlock() {
        _state.value.isLocked = false
        _state.value.toastMessage = "Image unlocked"
    }
    
    fun clearToast() {
        _state.value.toastMessage = null
    }
    
    fun resetTransform() {
        viewModelScope.launch {
            _state.value.updateScale(_state.value.minScale)
            _state.value.updateOffset(Offset.Zero)
        }
    }
    
    fun setLoading(isLoading: Boolean) {
        _state.value.isLoading = isLoading
    }
    
    fun setError(error: String?) {
        _state.value.error = error
        _state.value.isLoading = false
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