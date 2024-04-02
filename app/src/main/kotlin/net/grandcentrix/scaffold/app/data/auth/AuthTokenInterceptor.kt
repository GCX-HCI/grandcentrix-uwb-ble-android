package net.grandcentrix.scaffold.app.data.auth

import kotlinx.coroutines.runBlocking
import net.grandcentrix.scaffold.app.usecases.auth.GetCredentialsUseCase
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An Interceptor that gets the [net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials] via
 * the specified use case and adds the [net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials.accessToken]
 * to every request that goes through OKHTTP clients using this interceptor.
 */
internal class AuthTokenInterceptor(
    val getCredentials: GetCredentialsUseCase
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = runBlocking {
            getCredentials().successOrNull?.accessToken
        }

        return chain.proceed(
            chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        )
    }
}
