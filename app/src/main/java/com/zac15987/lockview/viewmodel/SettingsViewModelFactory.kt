package com.zac15987.lockview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zac15987.lockview.data.language.LanguageRepository
import com.zac15987.lockview.data.lockedcontrols.LockedControlsRepository
import com.zac15987.lockview.data.theme.ThemeRepository

class SettingsViewModelFactory(
    private val themeRepository: ThemeRepository,
    private val languageRepository: LanguageRepository,
    private val lockedControlsRepository: LockedControlsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(themeRepository, languageRepository, lockedControlsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}