package net.grandcentrix.scaffold.app.ui.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.scaffold.app.domain.auth.AuthorizationState
import net.grandcentrix.scaffold.app.domain.auth.ProfileData
import net.grandcentrix.scaffold.app.usecases.auth.GetUserProfileUseCase
import net.grandcentrix.scaffold.app.usecases.auth.LoginUseCase
import net.grandcentrix.scaffold.app.usecases.auth.LogoutUseCase
import net.grandcentrix.scaffold.app.usecases.auth.ObserveCredentialsUseCase

class AccountViewModel(
    private val login: LoginUseCase,
    private val observeCredentials: ObserveCredentialsUseCase,
    private val logout: LogoutUseCase,
    private val getUserInfo: GetUserProfileUseCase,
) : ViewModel() {

    private val credentialsFlow by lazy { observeCredentials() }

    val authState: Flow<AuthorizationState> = credentialsFlow
        .map { credentials ->
            credentials
                ?.let { AuthorizationState.LoggedIn }
                ?: AuthorizationState.LoggedOut
        }

    val profileData: Flow<ProfileData?> = credentialsFlow.map { credentials ->
        // TODO Show helpful error in UI to ensure good UX
        credentials?.accessToken?.run {
            getUserInfo(this).successOrNull
        }
    }

    fun onLogin(context: Context) {
        login(context)
    }

    fun onLogout() {
        logout()
    }
}
