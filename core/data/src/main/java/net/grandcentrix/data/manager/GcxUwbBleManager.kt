package net.grandcentrix.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.data.model.toGcxBleConnectionState
import net.grandcentrix.uwb.controlee.GcxUwbControlee

interface UwbBleManager {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>

    fun startRanging()
}
class GcxUwbBleManager(
    uwbManager: UwbManager,
    private val bleManager: BleManager,
    private val bleScanner: BleScanner
) : UwbBleManager {

    private val gcxUwbControlee = GcxUwbControlee(
        uwbManager = uwbManager,
        resultChannel = bleManager.resultChannel,
        bleClient = bleManager.clientController
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }

    override fun startRanging() {
        gcxUwbControlee.startRanging()
    }
}
