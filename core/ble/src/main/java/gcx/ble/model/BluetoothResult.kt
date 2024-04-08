package gcx.ble.model

import java.util.UUID

data class BluetoothResult(
    val uuid: UUID,
    val data: ByteArray?,
    val status: Int,
)
