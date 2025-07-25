package com.zac15987.lockview.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zac15987.lockview.data.theme.ThemeRepository

class ThemeViewModelFactory(private val themeRepository: ThemeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(themeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}