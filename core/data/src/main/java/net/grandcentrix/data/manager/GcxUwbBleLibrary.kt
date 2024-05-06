package net.grandcentrix.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.provider.UUIDProvider
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.ble.scanner.GcxBleScanner
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.data.model.toGcxBleConnectionState
import net.grandcentrix.uwb.controlee.GcxUwbControlee
import net.grandcentrix.uwb.controlee.UwbControlee

interface UwbBleLibrary {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>

    fun startRanging(): Flow<RangingResult>
}

class GcxUwbBleLibrary(
    context: Context,
    uuidProvider: UUIDProvider = UUIDProvider()
) : UwbBleLibrary {

    private val bleManager: BleManager = GcxBleManager(context, uuidProvider)
    private val bleScanner: BleScanner = GcxBleScanner(context)

    private val ubwControlee: UwbControlee = GcxUwbControlee(
        uwbManager = UwbManager.createInstance(context),
        bleMessages = bleManager.bleMessages,
        bleMessagingClient = bleManager.bleMessagingClient
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }

    override fun startRanging(): Flow<RangingResult> = ubwControlee.startRanging()
}
