package com.zac15987.lockview.data.theme

import androidx.annotation.StringRes
import com.zac15987.lockview.R

enum class ThemePreference(@StringRes val displayNameResId: Int) {
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark),
    SYSTEM(R.string.theme_system)
}