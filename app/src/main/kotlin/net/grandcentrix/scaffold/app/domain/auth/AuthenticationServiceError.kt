package net.grandcentrix.scaffold.app.domain.auth

sealed class AuthenticationServiceError : Throwable() {

    data class UnknownException(val error: Throwable) : AuthenticationServiceError()

    object EmptyPayloadException : AuthenticationServiceError()
    object AuthCredentialsException : AuthenticationServiceError()
    object NoUserIdException : AuthenticationServiceError()
    object NoIdTokenException : AuthenticationServiceError()
}
