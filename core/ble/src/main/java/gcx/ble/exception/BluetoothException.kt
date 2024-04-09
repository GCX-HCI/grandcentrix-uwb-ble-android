package gcx.ble.exception

sealed class BluetoothException(override val message: String?): Exception() {
    data object BluetoothDisabledException : BluetoothException("BLE Adapter is not enabled")

    data object ServiceDiscoveryFailedException: BluetoothException("Service discovery failed")
}

