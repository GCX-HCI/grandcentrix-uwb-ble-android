package net.grandcentrix.uwbBleAndroid.model

import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.model.GcxScanResult

data class BleScanResult(
    val gcxScanResult: GcxScanResult,
    val address: String,
    val connectionState: ConnectionState
)
fun GcxScanResult.toBleScanResult(
    connectionState: ConnectionState = ConnectionState.Disconnected
): BleScanResult = BleScanResult(
    gcxScanResult = this,
    address = address,
    connectionState = connectionState
)
