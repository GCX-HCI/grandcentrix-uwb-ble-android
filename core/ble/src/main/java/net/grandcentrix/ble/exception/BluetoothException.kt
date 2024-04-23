package net.grandcentrix.ble.exception

sealed class BluetoothException(override val message: String?) : Exception() {
    data object BluetoothDisabledException : BluetoothException("BLE Adapter is not enabled")

    data object ServiceDiscoveryFailedException : BluetoothException("Service discovery failed")

    data object BluetoothTimeoutException : BluetoothException("BLE action timeout")

    data object ServiceNotSupportedException :
        BluetoothException("The given service is not supported")

    data class BluetoothNullPointerException(
        val className: String
    ) : BluetoothException("$className is null")
}
