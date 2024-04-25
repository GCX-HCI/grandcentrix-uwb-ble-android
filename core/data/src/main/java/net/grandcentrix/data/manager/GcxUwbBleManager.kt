package net.grandcentrix.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.data.model.toGcxBleConnectionState

interface UwbBleManager {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>
}
class GcxUwbBleManager(
    private val bleManager: BleManager,
    private val bleScanner: BleScanner
) : UwbBleManager {
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }
}
