package net.grandcentrix.lib.uwb.exception

sealed class UwbException(override val message: String?) : Exception() {

    data object DeciveConfigNullException : UwbException("Didn't receive compliant device config")

    data object StartCommandNullException : UwbException(
        "Didn't receive command to start uwb session from controller!"
    )
}
