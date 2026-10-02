package com.zac15987.lockview.data.puremode

import androidx.annotation.StringRes
import com.zac15987.lockview.R

enum class PureModePreference(@param:StringRes val displayNameResId: Int) {
    ENABLED(R.string.pure_mode_enabled),
    DISABLED(R.string.pure_mode_disabled)
}
