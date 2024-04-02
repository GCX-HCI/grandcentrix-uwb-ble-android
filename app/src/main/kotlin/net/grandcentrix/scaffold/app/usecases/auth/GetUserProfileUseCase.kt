package net.grandcentrix.scaffold.app.usecases.auth

import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.app.data.auth.AccountRepository
import net.grandcentrix.scaffold.app.domain.auth.ProfileData

class GetUserProfileUseCase(private val repository: AccountRepository) {

    suspend operator fun invoke(accessToken: String): Either<Throwable, ProfileData> =
        repository.getUserProfile(accessToken)
}
