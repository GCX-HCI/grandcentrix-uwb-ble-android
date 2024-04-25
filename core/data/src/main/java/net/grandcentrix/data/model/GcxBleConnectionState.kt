package net.grandcentrix.data.model

import net.grandcentrix.ble.manager.ConnectionState

enum class GcxBleConnectionState {
    CONNECTED,
    DISCONNECTED,
    SERVICES_DISCOVERED
}

fun ConnectionState.toGcxBleConnectionState(): GcxBleConnectionState {
    return when (this) {
        ConnectionState.CONNECTED -> GcxBleConnectionState.CONNECTED
        ConnectionState.DISCONNECTED -> GcxBleConnectionState.DISCONNECTED
        ConnectionState.SERVICES_DISCOVERED -> GcxBleConnectionState.SERVICES_DISCOVERED
    }
}
