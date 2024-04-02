package net.grandcentrix.scaffold.app.usecases.auth

import net.grandcentrix.scaffold.app.data.auth.AuthRepository

class ObserveCredentialsUseCase(private val repository: AuthRepository) {

    operator fun invoke() = repository.observeCredentials()
}
