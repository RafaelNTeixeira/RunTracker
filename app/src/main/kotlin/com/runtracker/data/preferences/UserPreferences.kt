package com.runtracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store = context.dataStore

    val loggedInUsername: Flow<String?> = store.data.map { it[KEY_USERNAME] }

    suspend fun setLoggedIn(username: String) {
        store.edit { it[KEY_USERNAME] = username }
    }

    suspend fun logout() {
        store.edit { it.remove(KEY_USERNAME) }
    }

    companion object {
        private val KEY_USERNAME = stringPreferencesKey("logged_in_username")
    }
}
