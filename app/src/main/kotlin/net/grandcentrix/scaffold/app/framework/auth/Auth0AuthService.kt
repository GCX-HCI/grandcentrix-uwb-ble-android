package net.grandcentrix.scaffold.app.framework.auth

import com.auth0.android.Auth0Exception
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.callback.Callback
import com.auth0.android.result.Credentials
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import net.grandcentrix.either.Either
import net.grandcentrix.either.Failure
import net.grandcentrix.either.Success
import net.grandcentrix.either.flatMap
import net.grandcentrix.scaffold.app.data.auth.AuthRepository
import net.grandcentrix.scaffold.app.domain.auth.AuthenticationServiceError.EmptyPayloadException
import net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials
import net.grandcentrix.scaffold.app.util.toAuth0Credentials
import net.grandcentrix.scaffold.app.util.toOAuthCredentials
import net.grandcentrix.scaffold.app.util.toSuspendedEither

class Auth0AuthService(
    private val manager: SecureCredentialsManager,
    private val authClient: AuthenticationAPIClient
) : AuthRepository {

    private val credentialsFlow: MutableStateFlow<OAuthCredentials?> by lazy {
        MutableStateFlow(
            runBlocking { getCredentials().successOrNull }
        )
    }

    override fun login(credentials: OAuthCredentials) {
        // TODO Check for and handle CredentialsManagerException in cases encryption didn't work
        manager.saveCredentials(credentials.toAuth0Credentials())
        credentialsFlow.tryEmit(credentials)
    }

    override fun logout() {
        manager.clearCredentials()
        credentialsFlow.tryEmit(null)
    }

    override suspend fun getCredentials(): Either<Throwable, OAuthCredentials> {
        return suspendedAuth0Callback<Credentials, CredentialsManagerException> {
            manager.getCredentials(it)
        }.map(Credentials::toOAuthCredentials)
    }

    override fun hasCredentials(): Boolean = manager.hasValidCredentials()

    override fun observeCredentials(): Flow<OAuthCredentials?> = credentialsFlow

    override suspend fun renewAuthentication(): Either<Throwable, OAuthCredentials> {
        return getCredentials()
            .flatMap {
                authClient
                    .renewAuth(it.refreshToken ?: "")
                    .toSuspendedEither()
            }
            .onSuccess(manager::saveCredentials)
            .map(Credentials::toOAuthCredentials)
            .onSuccess(credentialsFlow::tryEmit)
    }

    private suspend fun <T, U : Auth0Exception> suspendedAuth0Callback(
        block: (Callback<T, U>) -> Unit
    ): Either<Throwable, T> =
        suspendCoroutine { continuation ->
            block(object : Callback<T, U> {
                override fun onFailure(error: U) {
                    continuation.resume(Failure(error))
                }

                override fun onSuccess(result: T) {
                    if (result == null) {
                        continuation.resume(Failure(EmptyPayloadException))
                    } else {
                        continuation.resume(Success(result))
                    }
                }
            })
        }
}
