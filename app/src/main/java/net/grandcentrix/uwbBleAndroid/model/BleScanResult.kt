package net.grandcentrix.uwbBleAndroid.model

import android.bluetooth.BluetoothDevice
import net.grandcentrix.api.ble.model.ConnectionState
import net.grandcentrix.api.ble.model.GcxScanResult

data class BleScanResult(
    val scanResult: GcxScanResult,
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
)
fun GcxScanResult.toBleDevice(): BleScanResult = BleScanResult(
    scanResult = this,
    bluetoothDevice = this.androidScanResult.device,
    connectionState = ConnectionState.Disconnected
)

fun ConnectionState.toBleDevice(scanResult: GcxScanResult): BleScanResult = BleScanResult(
    scanResult = scanResult,
    bluetoothDevice = scanResult.androidScanResult.device,
    connectionState = this
)
