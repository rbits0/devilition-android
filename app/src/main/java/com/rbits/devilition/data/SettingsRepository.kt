package com.rbits.devilition.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Settings(
    val timeBonusEnabled: Boolean = true
)

private object SettingsKeys {
    val TIME_BONUS_ENABLED = booleanPreferencesKey("time_bonus_enabled")
}

interface ISettingsRepository {
    val settingsFlow: Flow<Settings>

    suspend fun setTimeBonusEnabled(value: Boolean)
}

class SettingsRepository(
    private val settingsStore: DataStore<Preferences>
) : ISettingsRepository {
    override val settingsFlow = settingsStore.data
        .map { preferences ->
            val timeBonusEnabled = preferences[SettingsKeys.TIME_BONUS_ENABLED] ?: true

            Settings(timeBonusEnabled)
        }

    override suspend fun setTimeBonusEnabled(value: Boolean) {
        settingsStore.edit { preferences ->
            preferences[SettingsKeys.TIME_BONUS_ENABLED] = value
        }
    }
}

