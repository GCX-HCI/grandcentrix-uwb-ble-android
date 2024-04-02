package net.grandcentrix.scaffold.app.usecases.auth

import net.grandcentrix.scaffold.app.data.auth.AuthRepository

class CheckCredentialsExistUseCase(private val repository: AuthRepository) {

    operator fun invoke(): Boolean = repository.hasCredentials()
}
