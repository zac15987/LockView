package com.zac15987.lockview.utils

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

/**
 * Extracts a single shared image Uri from an ACTION_SEND intent.
 *
 * Returns null when the intent is not an image share or carries no usable Uri.
 * Some apps only populate [Intent.getClipData] instead of [Intent.EXTRA_STREAM],
 * so both are checked.
 *
 * The read grant comes from the sender's FLAG_GRANT_READ_URI_PERMISSION and lives
 * as long as this activity, so the Uri must not be persisted.
 */
fun Intent.extractSharedImageUri(): Uri? {
    if (action != Intent.ACTION_SEND) return null
    if (type?.startsWith("image/") != true) return null

    return IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java)
        ?: clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
}
