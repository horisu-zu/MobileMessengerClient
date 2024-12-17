package com.example.testapp.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreUtil(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

        val THEME_KEY = booleanPreferencesKey("theme")
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val CURRENT_USER_ID = stringPreferencesKey("user_id")
    }

    fun getTheme(isSystemDarkTheme: Boolean): Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[THEME_KEY] ?: isSystemDarkTheme
        }

    suspend fun saveTheme(isDarkThemeEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = isDarkThemeEnabled
        }
    }

    fun getAccessToken(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            val token = preferences[ACCESS_TOKEN_KEY]
            token
        }

    fun getRefreshToken(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            val token = preferences[REFRESH_TOKEN_KEY]
            token
        }

    fun getUserId(): Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_ID]
        }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun saveUserId(userId: String) {
        Log.d("DataStoreUtil", "Saving User Id: $userId")
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(CURRENT_USER_ID)
        }
    }
}