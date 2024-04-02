package net.grandcentrix.scaffold.app.util

import com.auth0.android.Auth0Exception
import com.auth0.android.callback.Callback
import com.auth0.android.request.Request
import com.auth0.android.result.Credentials
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import net.grandcentrix.either.Either
import net.grandcentrix.either.Failure
import net.grandcentrix.either.Success
import net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials

/**
 * Turns an Auth0 [Request] callback into a suspended coroutine.
 *
 * Instead of a Request instance, this returns an [Either]
 * with [Failure] emitted from [Callback.onFailure]
 * and [Success] emitted from [Callback.onSuccess].
 */
internal suspend fun <T, U : Auth0Exception> Request<T, U>.toSuspendedEither(): Either<U, T> {
    return suspendCoroutine { continuation ->
        start(object : Callback<T, U> {
            override fun onFailure(error: U) {
                continuation.resume(Failure(error))
            }

            override fun onSuccess(result: T) {
                continuation.resume(Success(result))
            }
        })
    }
}

internal fun Credentials.toOAuthCredentials(): OAuthCredentials = OAuthCredentials(
    userIdToken = idToken,
    accessToken = accessToken,
    refreshToken = refreshToken,
    tokenType = type,
    tokenExpiration = expiresAt
)

internal fun OAuthCredentials.toAuth0Credentials(): Credentials = Credentials(
    idToken = userIdToken,
    accessToken = accessToken,
    type = tokenType,
    refreshToken = refreshToken,
    expiresAt = tokenExpiration,
    scope = null
)
