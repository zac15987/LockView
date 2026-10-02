package com.zac15987.lockview.data.panrange

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.panRangeDataStore: DataStore<Preferences> by preferencesDataStore(name = "pan_range_preferences")

/**
 * Stores how much of the image (in percent, per axis) panning must leave on screen.
 */
class PanRangeRepository(private val context: Context) {

    private val minVisiblePercentKey = intPreferencesKey("min_visible_percent")

    val minVisiblePercent: Flow<Int> = context.panRangeDataStore.data
        .map { preferences ->
            (preferences[minVisiblePercentKey] ?: DEFAULT_PERCENT).coerceIn(MIN_PERCENT, MAX_PERCENT)
        }

    suspend fun setMinVisiblePercent(percent: Int) {
        context.panRangeDataStore.edit { preferences ->
            preferences[minVisiblePercentKey] = percent.coerceIn(MIN_PERCENT, MAX_PERCENT)
        }
    }

    companion object {
        // Below 5% the image can all but vanish; the pan detector covers the whole screen, so
        // the visible sliver never needs to be large enough to grab
        const val MIN_PERCENT = 5
        const val MAX_PERCENT = 50
        const val STEP_PERCENT = 5
        const val DEFAULT_PERCENT = 10
    }
}
