package gcx.grandcentrix_uwb_ble_android.model

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import gcx.ble.manager.ConnectionState

data class GcxBleDevice(
    val bluetoothDevice: BluetoothDevice,
    val connectionState: ConnectionState
)

fun ScanResult.toGcxBleDevice() = GcxBleDevice(
    bluetoothDevice = device,
    connectionState = ConnectionState.DISCONNECTED
)
