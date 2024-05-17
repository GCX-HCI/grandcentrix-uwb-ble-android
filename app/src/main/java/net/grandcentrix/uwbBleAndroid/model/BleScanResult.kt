package net.grandcentrix.uwbBleAndroid.model

import android.bluetooth.BluetoothDevice
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.model.GcxScanResult

data class BleScanResult(
    val scanResult: GcxScanResult,
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
)
fun GcxScanResult.toBleScanResult(): BleScanResult = BleScanResult(
    scanResult = this,
    bluetoothDevice = this.androidScanResult.device,
    connectionState = ConnectionState.Disconnected
)

fun ConnectionState.toBleScanResult(scanResult: GcxScanResult): BleScanResult = BleScanResult(
    scanResult = scanResult,
    bluetoothDevice = scanResult.androidScanResult.device,
    connectionState = this
)
