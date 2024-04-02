package net.grandcentrix.scaffold.app.data.auth

import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.app.domain.auth.ProfileData

interface AccountRepository {

    suspend fun getUserProfile(accessToken: String): Either<Throwable, ProfileData>
}
