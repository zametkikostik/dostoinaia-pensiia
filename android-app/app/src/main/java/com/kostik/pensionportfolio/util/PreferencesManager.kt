package com.kostik.pensionportfolio.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Хранилище настроек (DataStore)
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    
    companion object {
        val TINKOFF_TOKEN_KEY = stringPreferencesKey("tinkoff_token")
        val PORTFOLIO_ID_KEY = stringPreferencesKey("portfolio_id")
    }
    
    val tinkoffTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TINKOFF_TOKEN_KEY]
    }
    
    suspend fun saveTinkoffToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TINKOFF_TOKEN_KEY] = token
        }
    }
    
    suspend fun clearTinkoffToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TINKOFF_TOKEN_KEY)
        }
    }
    
    suspend fun savePortfolioId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[PORTFOLIO_ID_KEY] = id
        }
    }
}
