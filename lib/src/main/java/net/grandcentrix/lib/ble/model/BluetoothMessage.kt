package net.grandcentrix.lib.ble.model

import java.util.UUID

data class BluetoothMessage(
    val uuid: UUID,
    val data: ByteArray?,
    val status: Int
)
