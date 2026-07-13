package com.zac15987.lockview.data.puremode

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pureModeDataStore: DataStore<Preferences> by preferencesDataStore(name = "pure_mode_preferences")

class PureModeRepository(private val context: Context) {

    private val pureModeKey = stringPreferencesKey("pure_mode_preference")

    val pureModePreference: Flow<PureModePreference> = context.pureModeDataStore.data
        .map { preferences ->
            val preferenceName = preferences[pureModeKey] ?: PureModePreference.DISABLED.name
            try {
                PureModePreference.valueOf(preferenceName)
            } catch (e: IllegalArgumentException) {
                PureModePreference.DISABLED
            }
        }

    suspend fun setPureModePreference(preference: PureModePreference) {
        context.pureModeDataStore.edit { preferences ->
            preferences[pureModeKey] = preference.name
        }
    }
}
