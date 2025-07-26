package com.zac15987.lockview.data

import androidx.annotation.StringRes
import com.zac15987.lockview.R

data class DonationOption(
    @StringRes val displayNameResId: Int,
    val url: String
) {
    companion object {
        val PAYPAL = DonationOption(
            displayNameResId = R.string.paypal,
            url = "https://paypal.com"
        )
        
        val KOFI = DonationOption(
            displayNameResId = R.string.kofi,
            url = "https://ko-fi.com"
        )
        
        fun all() = listOf(PAYPAL, KOFI)
    }
}