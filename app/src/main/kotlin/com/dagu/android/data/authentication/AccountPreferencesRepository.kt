package com.dagu.android.data.authentication

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountPreferencesRepository @Inject constructor(@ApplicationContext val context: Context) {

    companion object {
        val KEY_AUTH_TOKEN = preferencesKey<String>("auth_token")
        val KEY_REFRESH_TOKEN = preferencesKey<String>("refresh_token")
        val KEY_OLO_PROVIDER_TOKEN = preferencesKey<String>("olo_provider_token")
        val KEY_OLO_AUTH_TOKEN = preferencesKey<String>("olo_auth_token")
    }

    private val dataStore: DataStore<Preferences> = context.applicationContext.createDataStore(
        name = "shake_shack_account_preferences"
    )

    val authToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_TOKEN]
        }

    suspend fun updateAuthToken(authToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTH_TOKEN] = authToken
        }
    }

    val refreshToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_REFRESH_TOKEN] ?: ""
        }

    suspend fun updateRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    val oloProviderToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_OLO_PROVIDER_TOKEN]
        }

    suspend fun updateOloProviderToken(oloProviderToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OLO_PROVIDER_TOKEN] = oloProviderToken
        }
    }

    val oloAuthToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_OLO_AUTH_TOKEN]
    }

    suspend fun updateOloAuthTokem(oloAuthToken: String) {
        dataStore.edit { preferences ->
            preferences[KEY_OLO_AUTH_TOKEN] = oloAuthToken
        }
    }
}