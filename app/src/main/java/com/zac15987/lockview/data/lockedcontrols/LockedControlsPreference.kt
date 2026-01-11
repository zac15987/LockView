package com.zac15987.lockview.data.lockedcontrols

import androidx.annotation.StringRes
import com.zac15987.lockview.R

enum class LockedControlsPreference(@StringRes val displayNameResId: Int) {
    ENABLED(R.string.locked_controls_enabled),
    DISABLED(R.string.locked_controls_disabled)
}
