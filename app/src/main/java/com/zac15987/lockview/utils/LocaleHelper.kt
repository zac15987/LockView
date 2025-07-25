package com.zac15987.lockview.utils

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.zac15987.lockview.data.language.LanguagePreference
import java.util.Locale

object LocaleHelper {
    
    fun setLocale(context: Context, languagePreference: LanguagePreference): Context {
        return when (languagePreference) {
            LanguagePreference.SYSTEM -> resetToSystemLocale(context)
            else -> updateResources(context, languagePreference.localeCode)
        }
    }
    
    private fun resetToSystemLocale(context: Context): Context {
        // Get the actual system locale from the base application context
        val systemLocale = getSystemLocale(context)
        
        // Reset to system default
        Locale.setDefault(systemLocale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, clear application locales to use system default
            context.getSystemService(LocaleManager::class.java)?.applicationLocales = LocaleList.getEmptyLocaleList()
            context
        } else {
            // For older versions, create context with system locale
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(systemLocale)
            context.createConfigurationContext(configuration)
        }
    }
    
    private fun getSystemLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Get the actual system locale from system resources
            val systemResources = android.content.res.Resources.getSystem()
            systemResources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            // For older versions, get from system resources
            android.content.res.Resources.getSystem().configuration.locale
        }
    }
    
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = if (languageCode.contains("-")) {
            val parts = languageCode.split("-")
            Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
        } else {
            Locale.Builder().setLanguage(languageCode).build()
        }
        
        Locale.setDefault(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+, use LocaleManager
            context.getSystemService(LocaleManager::class.java)?.applicationLocales = 
                LocaleList.forLanguageTags(languageCode)
            context
        } else {
            // For older versions, update configuration
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        }
    }
    
    fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }
    
    fun wrap(context: Context, languagePreference: LanguagePreference): Context {
        return setLocale(context, languagePreference)
    }
}