package net.grandcentrix.api.ble.model

sealed interface ConnectionState {
    data object Connected : ConnectionState

    data object Disconnected : ConnectionState

    data class ServicesDiscovered(val gcxUwbDevice: GcxUwbDevice) : ConnectionState
}
