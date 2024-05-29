package net.grandcentrix.lib.ble.provider

import java.util.UUID
import net.grandcentrix.lib.ble.gatt.GcxGattClient

/**
 * This class supplies the UUIDs for the service and characteristics required to communicate
 * with a UWB device using the UART (Universal Asynchronous Receiver-Transmitter) profile.
 *
 * @property serviceUUID The UUID of the UART service.
 * @property rxUUID The UUID of the UART RX (receive) characteristic.
 * @property txUUID The UUID of the UART TX (transmit) characteristic.
 */
class UUIDProvider(
    val serviceUUID: UUID = UUID.fromString(GcxGattClient.UART_SERVICE),
    val rxUUID: UUID = UUID.fromString(GcxGattClient.UART_RX_CHARACTERISTIC),
    val txUUID: UUID = UUID.fromString(GcxGattClient.UART_TX_CHARACTERISTIC)
)
