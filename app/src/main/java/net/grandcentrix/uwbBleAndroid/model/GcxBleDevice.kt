package net.grandcentrix.uwbBleAndroid.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import net.grandcentrix.data.model.GcxBleConnectionState

data class GcxBleDevice(
    val bluetoothDevice: BluetoothDevice,
    val connectionState: GcxBleConnectionState
)

fun ScanResult.toGcxBleDevice() = GcxBleDevice(
    bluetoothDevice = device,
    connectionState = GcxBleConnectionState.DISCONNECTED
)
