package net.grandcentrix.scaffold.app.framework.auth

import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.result.UserProfile
import net.grandcentrix.either.Either
import net.grandcentrix.scaffold.app.data.auth.AccountRepository
import net.grandcentrix.scaffold.app.domain.auth.AuthenticationServiceError
import net.grandcentrix.scaffold.app.domain.auth.ProfileData
import net.grandcentrix.scaffold.app.util.toSuspendedEither

class Auth0AccountService(private val apiClient: AuthenticationAPIClient) : AccountRepository {

    override suspend fun getUserProfile(
        accessToken: String
    ): Either<Throwable, ProfileData> {
        return apiClient.userInfo(accessToken)
            .toSuspendedEither()
            // TODO AuthenticationException may have different origins. Check and map properly!
            .mapFailure { AuthenticationServiceError.AuthCredentialsException }
            .map { it.toProfileData() }
    }

    private fun UserProfile.toProfileData(): ProfileData = ProfileData(
        userId = getId(),
        emailAddress = email,
        profileImageUrl = pictureURL
    )
}
