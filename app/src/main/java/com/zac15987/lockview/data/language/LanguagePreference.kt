package com.zac15987.lockview.data.language

import androidx.annotation.StringRes
import com.zac15987.lockview.R

enum class LanguagePreference(
    val localeCode: String,
    @StringRes val displayNameResId: Int
) {
    SYSTEM("", R.string.language_system),
    ENGLISH("en", R.string.language_english),
    TRADITIONAL_CHINESE("zh-TW", R.string.language_traditional_chinese);
    
    companion object {
        fun fromLocaleCode(code: String): LanguagePreference {
            return values().find { it.localeCode == code } ?: SYSTEM
        }
    }
}