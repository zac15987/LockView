package com.zac15987.lockview

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.zac15987.lockview.data.language.LanguagePreference
import com.zac15987.lockview.utils.LocaleHelper

class LockViewApplication : Application() {
    
    override fun attachBaseContext(base: Context) {
        // Use SharedPreferences for immediate access during attachBaseContext
        val sharedPrefs = base.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "")
        val languagePreference = if (languageCode.isNullOrEmpty()) {
            LanguagePreference.SYSTEM
        } else {
            LanguagePreference.fromLocaleCode(languageCode)
        }
        super.attachBaseContext(LocaleHelper.wrap(base, languagePreference))
    }
    
    override fun onCreate() {
        super.onCreate()
    }
}