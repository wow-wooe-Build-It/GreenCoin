package com.greencoins.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

object ThemePreferences {
    val isDarkThemeState: MutableState<Boolean> = mutableStateOf(true)

    suspend fun load(context: Context) {
        val prefs = context.dataStore.data.first()
        isDarkThemeState.value = prefs[booleanPreferencesKey("is_dark_theme")] ?: true
    }

    suspend fun setDarkTheme(context: Context, value: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("is_dark_theme")] = value
        }
        isDarkThemeState.value = value
    }
}
