package net.grandcentrix.lib.ble.model

/**
 * Represents the various states of a GATT (Generic Attribute Profile) connection to a bluetooth device.
 */
sealed interface ConnectionState {

    /**
     * Represents the state when the connection to the bluetooth device is successfully established.
     */
    data object Connected : ConnectionState

    /**
     * Represents the state when the connection to the bluetooth device is disconnected.
     */
    data object Disconnected : ConnectionState

    /**
     * Represents the state when the services of the bluetooth device have been discovered.
     *
     * @property gcxUwbDevice The discovered GCX UWB device, containing detailed information about the services and characteristics.
     */
    data class ServicesDiscovered(val gcxUwbDevice: GcxUwbDevice) : ConnectionState
}
