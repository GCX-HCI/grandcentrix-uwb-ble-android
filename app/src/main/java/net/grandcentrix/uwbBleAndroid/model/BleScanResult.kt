package net.grandcentrix.uwbBleAndroid.model

import android.bluetooth.BluetoothDevice
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.model.GcxScanResult

data class BleScanResult(
    val gcxScanResult: GcxScanResult,
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
)
fun GcxScanResult.toBleScanResult(
    connectionState: ConnectionState = ConnectionState.Disconnected
): BleScanResult = BleScanResult(
    gcxScanResult = this,
    bluetoothDevice = this.androidScanResult.device,
    connectionState = connectionState
)
