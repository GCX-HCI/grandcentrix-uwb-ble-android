package net.grandcentrix.uwbBleAndroid.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import net.grandcentrix.lib.ble.model.ConnectionState

data class GcxBleDevice(
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
)

fun ScanResult.toGcxBleDevice() = GcxBleDevice(
    bluetoothDevice = device,
    connectionState = ConnectionState.Disconnected
)
