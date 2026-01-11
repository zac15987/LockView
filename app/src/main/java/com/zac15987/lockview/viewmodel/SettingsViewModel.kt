package com.zac15987.lockview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zac15987.lockview.data.language.LanguagePreference
import com.zac15987.lockview.data.language.LanguageRepository
import com.zac15987.lockview.data.lockedcontrols.LockedControlsPreference
import com.zac15987.lockview.data.lockedcontrols.LockedControlsRepository
import com.zac15987.lockview.data.theme.ThemePreference
import com.zac15987.lockview.data.theme.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository,
    private val lockedControlsRepository: LockedControlsRepository
) : ViewModel() {
    
    val themePreference: StateFlow<ThemePreference> = themeRepository.themePreference
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = ThemePreference.SYSTEM
        )
    
    val languagePreference: StateFlow<LanguagePreference> = languageRepository.languagePreference
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = LanguagePreference.SYSTEM
        )

    val lockedControlsPreference: StateFlow<LockedControlsPreference> = lockedControlsRepository.lockedControlsPreference
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = LockedControlsPreference.DISABLED
        )

    private val _languageChanged = MutableStateFlow(false)
    val languageChanged: StateFlow<Boolean> = _languageChanged.asStateFlow()
    
    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            themeRepository.setThemePreference(theme)
        }
    }
    
    fun setLanguagePreference(language: LanguagePreference) {
        viewModelScope.launch {
            languageRepository.setLanguagePreference(language)
            _languageChanged.value = true
        }
    }

    fun setLockedControlsPreference(preference: LockedControlsPreference) {
        viewModelScope.launch {
            lockedControlsRepository.setLockedControlsPreference(preference)
        }
    }

    fun onLanguageChangeHandled() {
        _languageChanged.value = false
    }
}