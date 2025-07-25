package com.zac15987.lockview.data.language

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

class LanguageRepository(private val context: Context) {
    
    private val languageKey = stringPreferencesKey("language_preference")
    private val sharedPrefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    
    val languagePreference: Flow<LanguagePreference> = context.languageDataStore.data
        .map { preferences ->
            val languageName = preferences[languageKey] ?: LanguagePreference.SYSTEM.name
            try {
                LanguagePreference.valueOf(languageName)
            } catch (e: IllegalArgumentException) {
                LanguagePreference.SYSTEM
            }
        }
    
    suspend fun setLanguagePreference(language: LanguagePreference) {
        // Save to both DataStore and SharedPreferences for Application class access
        context.languageDataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
        
        // Also save to SharedPreferences for immediate access during app startup
        sharedPrefs.edit()
            .putString("language_code", language.localeCode)
            .apply()
    }
}