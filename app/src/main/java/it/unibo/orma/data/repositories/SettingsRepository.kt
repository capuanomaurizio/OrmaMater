package it.unibo.orma.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { Light, Dark, System }

/**
 * Persistenza chiave-valore con DataStore (preferenze utente).
 * Complementare a Room, che gestisce i dati strutturati.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val AVATAR_KEY = stringPreferencesKey("avatar_uri")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        try {
            ThemeMode.valueOf(prefs[THEME_KEY] ?: ThemeMode.System.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.System
        }
    }

    val username: Flow<String?> = dataStore.data.map { it[USERNAME_KEY] }
    val avatarUri: Flow<String?> = dataStore.data.map { it[AVATAR_KEY] }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[THEME_KEY] = mode.name }
    }

    suspend fun setUsername(name: String) {
        dataStore.edit { it[USERNAME_KEY] = name }
    }

    suspend fun setAvatarUri(uri: String) {
        dataStore.edit { it[AVATAR_KEY] = uri }
    }
}
