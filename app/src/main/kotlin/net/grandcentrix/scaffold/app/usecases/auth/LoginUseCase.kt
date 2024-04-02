package net.grandcentrix.scaffold.app.usecases.auth

import android.content.Context
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import net.grandcentrix.scaffold.app.data.auth.AuthRepository
import net.grandcentrix.scaffold.app.util.toOAuthCredentials

class LoginUseCase(
    private val auth0: Auth0,
    private val repository: AuthRepository,
    private val oAuthScheme: String,
    private val oAuthScope: String
) {

    /**
     * @param context The Activity context to launch the web based login flow.
     * Technically an Application Context would work but we have to add the FLAG_ACTIVITY_NEW_TASK
     * to the launching intent in this case. Doing so would move the web based login flow to another
     * task stack which is basically another backstack.
     * So we want to avoid it by requesting the current Activity context as parameter.
     */
    operator fun invoke(context: Context) {
        // Setup the WebAuthProvider, using the custom scheme and scope.
        WebAuthProvider.login(auth0)
            .withScheme(oAuthScheme)
            .withScope(oAuthScope)
            // Launch the authentication passing the callback where the results will be received
            .start(
                context,
                object : Callback<Credentials, AuthenticationException> {
                    // Called when there is an authentication failure
                    override fun onFailure(error: AuthenticationException) {
                        // TODO Do something with the exception
                    }

                    // Called when authentication completed successfully
                    override fun onSuccess(result: Credentials) {
                        repository.login(result.toOAuthCredentials())
                    }
                }
            )
    }
}
