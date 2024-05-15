package net.grandcentrix.lib.ble.provider

import java.util.UUID
import net.grandcentrix.lib.ble.manager.GcxBleManager

class UUIDProvider(
    val serviceUUID: UUID = UUID.fromString(GcxBleManager.UART_SERVICE),
    val rxUUID: UUID = UUID.fromString(GcxBleManager.UART_RX_CHARACTERISTIC),
    val txUUID: UUID = UUID.fromString(GcxBleManager.UART_TX_CHARACTERISTIC)
)
