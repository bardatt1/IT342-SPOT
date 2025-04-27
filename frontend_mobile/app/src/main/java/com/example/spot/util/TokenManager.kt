package com.example.spot.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages the storage and retrieval of JWT tokens and user information
 */
object TokenManager {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spot_prefs")
    
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = longPreferencesKey("user_id")
    private val USER_TYPE_KEY = stringPreferencesKey("user_type")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    
    /**
     * Initialize the TokenManager with the application context
     */
    private lateinit var dataStore: DataStore<Preferences>
    
    fun initialize(context: Context) {
        dataStore = context.dataStore
    }
    
    /**
     * Save the JWT token and user information from login response
     */
    suspend fun saveAuthData(token: String, userId: Long, userType: String, email: String, name: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_TYPE_KEY] = userType
            preferences[USER_EMAIL_KEY] = email
            preferences[USER_NAME_KEY] = name
        }
    }
    
    /**
     * Get the stored JWT token
     */
    fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }
    
    /**
     * Get the stored user ID
     */
    fun getUserId(): Flow<Long?> {
        return dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }
    
    /**
     * Get the stored user type (student, teacher, admin)
     */
    fun getUserType(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_TYPE_KEY]
        }
    }
    
    /**
     * Get the stored user email
     */
    fun getUserEmail(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }
    
    /**
     * Get the stored user name
     */
    fun getUserName(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }
    }
    
    /**
     * Clear all stored authentication data (logout)
     */
    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_TYPE_KEY)
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(USER_NAME_KEY)
        }
    }
}
