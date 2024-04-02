package net.grandcentrix.scaffold.app.domain.auth

import java.util.Date

/**
 * Base class for OAuth credentials which should always have a user id token, access token,
 * token type, and token expiration date.
 *
 * The design of this class is heavily influenced by how Auth0 handles credentials; further
 * generalisation might be in order when using different libraries.
 */
data class OAuthCredentials(
    val userIdToken: String,
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val tokenExpiration: Date,
)
