package bjoern.kinberger.gcx.ble.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

private const val TAG = "GCX_BLE_MANAGER"

interface BleManager {
    fun bluetoothAdapter(): BluetoothAdapter
}

class GcxBleManager(
    private val context: Context,
) : BleManager {
    val bluetoothAdapter: BluetoothAdapter

    override fun bluetoothAdapter(): BluetoothAdapter = bluetoothAdapter

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
    }
}
