package com.zac15987.lockview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zac15987.lockview.data.theme.ThemePreference
import com.zac15987.lockview.data.theme.ThemeRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(private val themeRepository: ThemeRepository) : ViewModel() {
    
    val themePreference: StateFlow<ThemePreference> = themeRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.SYSTEM
        )
    
    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            themeRepository.setThemePreference(theme)
        }
    }
}