package net.grandcentrix.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.data.model.toGcxBleConnectionState
import net.grandcentrix.uwb.controlee.GcxUwbControlee

interface UwbBleLibrary {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>

    @RequiresPermission(Manifest.permission.UWB_RANGING)
    fun startRanging(): Flow<RangingResult>
}

class GcxUwbBleLibrary(
    uwbManager: UwbManager,
    private val bleManager: BleManager,
    private val bleScanner: BleScanner
) : UwbBleLibrary {

    private val gcxUwbControlee = GcxUwbControlee(
        uwbManager = uwbManager,
        bleMessages = bleManager.bleMessages,
        bleMessagingClient = bleManager.bleMessagingClient
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }

    @RequiresPermission(Manifest.permission.UWB_RANGING)
    override fun startRanging(): Flow<RangingResult> = gcxUwbControlee.startRanging()
}
