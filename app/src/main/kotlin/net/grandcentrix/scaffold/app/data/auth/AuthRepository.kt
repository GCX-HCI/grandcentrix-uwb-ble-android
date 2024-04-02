package net.grandcentrix.scaffold.app.data.auth

import kotlinx.coroutines.flow.Flow
import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials

interface AuthRepository {

    fun login(credentials: OAuthCredentials)

    fun logout()

    suspend fun getCredentials(): Either<Throwable, OAuthCredentials>

    fun hasCredentials(): Boolean

    fun observeCredentials(): Flow<OAuthCredentials?>

    suspend fun renewAuthentication(): Either<Throwable, OAuthCredentials>
}
