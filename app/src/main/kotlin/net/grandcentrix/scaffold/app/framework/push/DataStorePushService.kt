package net.grandcentrix.scaffold.app.framework.push

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.scaffold.app.data.push.PushRepository

private val PREF_PUSH_TOKEN = stringPreferencesKey("PUSH_TOKEN")

class DataStorePushService(private val dataStore: DataStore<Preferences>) : PushRepository {

    override suspend fun onNewToken(newToken: String) {
        dataStore.edit { preferences ->
            preferences[PREF_PUSH_TOKEN] = newToken
        }
    }

    override fun getToken(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[PREF_PUSH_TOKEN]
        }
    }
}
