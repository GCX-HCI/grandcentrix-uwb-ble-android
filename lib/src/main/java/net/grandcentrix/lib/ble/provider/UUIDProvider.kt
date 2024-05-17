package net.grandcentrix.lib.ble.provider

import java.util.UUID
import net.grandcentrix.lib.ble.gatt.GcxGattClient

class UUIDProvider(
    val serviceUUID: UUID = UUID.fromString(GcxGattClient.UART_SERVICE),
    val rxUUID: UUID = UUID.fromString(GcxGattClient.UART_RX_CHARACTERISTIC),
    val txUUID: UUID = UUID.fromString(GcxGattClient.UART_TX_CHARACTERISTIC)
)
