package gcx.ble.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

interface BleManager {
    fun bluetoothAdapter(): BluetoothAdapter
}

class GcxBleManager(
    context: Context,
) : BleManager {
    private val bluetoothAdapter: BluetoothAdapter

    override fun bluetoothAdapter(): BluetoothAdapter = bluetoothAdapter

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
    }
}
