package net.grandcentrix.scaffold.app.usecases.auth

import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.app.data.auth.AuthRepository
import net.grandcentrix.scaffold.app.domain.auth.OAuthCredentials

class GetCredentialsUseCase(private val repository: AuthRepository) {

    suspend operator fun invoke(): Either<Throwable, OAuthCredentials> = repository.getCredentials()
}
