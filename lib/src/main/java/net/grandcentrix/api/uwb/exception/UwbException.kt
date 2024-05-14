package net.grandcentrix.api.uwb.exception

sealed class UwbException(override val message: String?) : Exception() {

    data object DeciveConfigNullException : UwbException("Didn't receive compliant device config")
}
