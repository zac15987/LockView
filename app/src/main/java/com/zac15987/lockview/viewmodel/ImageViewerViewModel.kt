package com.zac15987.lockview.viewmodel

import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        _state.update { it.copy(imageUri = uri, scale = 1f, offset = Offset.Zero) }
    }
    
    fun updateScale(scale: Float) {
        _state.update { it.copy(scale = scale.coerceIn(0.5f, 5f)) }
    }
    
    fun updateOffset(offset: Offset) {
        _state.update { it.copy(offset = offset) }
    }
    
    fun toggleLock() {
        val newLockState = !_state.value.isLocked
        _state.update { 
            it.copy(
                isLocked = newLockState, 
                toastMessage = if (newLockState) "Image locked" else "Image unlocked"
            ) 
        }
    }
    
    fun unlock() {
        _state.update { it.copy(isLocked = false, toastMessage = "Image unlocked") }
    }
    
    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
    
    fun resetTransform() {
        _state.update { it.copy(scale = 1f, offset = Offset.Zero) }
    }
    
    fun setLoading(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }
    
    fun setError(error: String?) {
        _state.update { it.copy(error = error, isLoading = false) }
    }
}