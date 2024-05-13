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
import net.grandcentrix.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.uwb.controlee.GcxUwbControlee
import net.grandcentrix.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.uwb.controlee.UwbControlee
import net.grandcentrix.uwb.model.RangingConfig

interface UwbBleLibrary {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    fun startRanging(): Flow<RangingResult>
}

class GcxUwbBleLibrary(
    context: Context,
    uuidProvider: UUIDProvider = UUIDProvider(),
    deviceConfigInterceptor: DeviceConfigInterceptor,
    phoneConfigInterceptor: PhoneConfigInterceptor,
    rangingConfig: RangingConfig
) : UwbBleLibrary {

    private val bleManager: BleManager = GcxBleManager(context, uuidProvider)
    private val bleScanner: BleScanner = GcxBleScanner(context)

    private val uwbControlee: UwbControlee = GcxUwbControlee(
        uwbManager = UwbManager.createInstance(context),
        bleMessagingClient = bleManager.bleMessagingClient,
        deviceConfigInterceptor = deviceConfigInterceptor,
        phoneConfigInterceptor = phoneConfigInterceptor,
        rangingConfig = rangingConfig
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    override fun startRanging(): Flow<RangingResult> = uwbControlee.startRanging()
}
