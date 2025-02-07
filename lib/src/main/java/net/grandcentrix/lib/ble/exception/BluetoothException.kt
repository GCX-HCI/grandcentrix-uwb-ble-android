package net.grandcentrix.lib.ble.exception

sealed class BluetoothException(override val message: String?) : Exception() {

    data object BluetoothMacAddressInvalidException : BluetoothException(
        "Device mac address was invalid!"
    )
    data object BluetoothDisabledException : BluetoothException("BLE Adapter is not enabled")

    data class ScanFailure(
        val reason: Int
    ) : BluetoothException("Bluetooth scan failure with reason: $reason")

    data object ServiceDiscoveryFailedException : BluetoothException("Service discovery failed")

    data object BluetoothTimeoutException : BluetoothException("BLE action timeout")

    data object ServiceNotSupportedException :
        BluetoothException("The given service is not supported")

    data class ConnectionFailure(
        val reason: Int
    ) : BluetoothException("Connection failure with reason $reason")
}
