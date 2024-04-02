package net.grandcentrix.scaffold.app.data.push

import kotlinx.coroutines.flow.Flow

interface PushRepository {

    suspend fun onNewToken(newToken: String)

    fun getToken(): Flow<String?>
}
