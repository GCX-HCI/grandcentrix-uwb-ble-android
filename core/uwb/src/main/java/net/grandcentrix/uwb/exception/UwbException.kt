package net.grandcentrix.uwb.exception

sealed class UwbException(override val message: String?) : Exception() {

    data object InitialisationFailure : UwbException("Failed to initialize UWB ranging")
}
