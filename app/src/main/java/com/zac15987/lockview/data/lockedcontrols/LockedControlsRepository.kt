package com.zac15987.lockview.data.lockedcontrols

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "locked_controls_preferences")

class LockedControlsRepository(private val context: Context) {

    private val lockedControlsKey = stringPreferencesKey("locked_controls_preference")

    val lockedControlsPreference: Flow<LockedControlsPreference> = context.dataStore.data
        .map { preferences ->
            val preferenceName = preferences[lockedControlsKey] ?: LockedControlsPreference.DISABLED.name
            try {
                LockedControlsPreference.valueOf(preferenceName)
            } catch (e: IllegalArgumentException) {
                LockedControlsPreference.DISABLED
            }
        }

    suspend fun setLockedControlsPreference(preference: LockedControlsPreference) {
        context.dataStore.edit { preferences ->
            preferences[lockedControlsKey] = preference.name
        }
    }
}
