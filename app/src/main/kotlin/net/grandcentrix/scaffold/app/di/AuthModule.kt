package net.grandcentrix.scaffold.app.di

import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.authentication.storage.Storage
import net.grandcentrix.scaffold.app.R
import net.grandcentrix.scaffold.app.data.auth.AccountRepository
import net.grandcentrix.scaffold.app.data.auth.AuthRepository
import net.grandcentrix.scaffold.app.framework.auth.Auth0AccountService
import net.grandcentrix.scaffold.app.framework.auth.Auth0AuthService
import net.grandcentrix.scaffold.app.ui.shared.AccountViewModel
import net.grandcentrix.scaffold.app.usecases.auth.CheckCredentialsExistUseCase
import net.grandcentrix.scaffold.app.usecases.auth.GetCredentialsUseCase
import net.grandcentrix.scaffold.app.usecases.auth.GetUserProfileUseCase
import net.grandcentrix.scaffold.app.usecases.auth.LoginUseCase
import net.grandcentrix.scaffold.app.usecases.auth.LogoutUseCase
import net.grandcentrix.scaffold.app.usecases.auth.ObserveCredentialsUseCase
import net.grandcentrix.scaffold.app.usecases.auth.RenewAuthenticationUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {

    single {
        Auth0(
            clientId = androidContext().getString(R.string.com_auth0_client_id),
            domain = androidContext().getString(R.string.com_auth0_domain)
        )
    }

    single<Storage> { SharedPreferencesStorage(context = androidContext()) }
    single { AuthenticationAPIClient(auth0 = get()) }
    single {
        SecureCredentialsManager(
            context = androidContext(),
            apiClient = get(),
            storage = get()
        )
    }

    single<AuthRepository> {
        Auth0AuthService(
            manager = get(),
            authClient = get()
        )
    }
    single<AccountRepository> { Auth0AccountService(apiClient = get()) }

    single {
        LoginUseCase(
            auth0 = get(),
            repository = get(),
            oAuthScheme = androidContext().getString(R.string.com_auth0_domain_scheme),
            oAuthScope = androidContext().getString(R.string.com_auth0_scope)
        )
    }
    single { LogoutUseCase(repository = get()) }
    single { CheckCredentialsExistUseCase(repository = get()) }
    single { GetCredentialsUseCase(repository = get()) }
    single { ObserveCredentialsUseCase(repository = get()) }
    single { GetUserProfileUseCase(repository = get()) }
    single { RenewAuthenticationUseCase(repository = get()) }

    viewModel {
        AccountViewModel(
            login = get(),
            logout = get(),
            observeCredentials = get(),
            getUserInfo = get()
        )
    }
}
