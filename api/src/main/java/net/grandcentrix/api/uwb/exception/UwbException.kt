package net.grandcentrix.uwb.exception

sealed class UwbException(override val message: String?) : Exception() {

    data object DeciveConfigNullException : UwbException("Didn't receive compliant device config")
}
