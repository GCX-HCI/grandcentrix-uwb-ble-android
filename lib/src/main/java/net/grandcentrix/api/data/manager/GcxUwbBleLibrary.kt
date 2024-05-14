package net.grandcentrix.api.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.grandcentrix.api.ble.manager.BleManager
import net.grandcentrix.api.ble.manager.GcxBleManager
import net.grandcentrix.api.ble.provider.UUIDProvider
import net.grandcentrix.api.ble.scanner.BleScanner
import net.grandcentrix.api.ble.scanner.GcxBleScanner
import net.grandcentrix.api.data.model.GcxBleConnectionState
import net.grandcentrix.api.data.model.toGcxBleConnectionState
import net.grandcentrix.api.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.api.uwb.controlee.GcxUwbControlee
import net.grandcentrix.api.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.api.uwb.controlee.UwbControlee
import net.grandcentrix.api.uwb.model.RangingConfig

interface UwbBleLibrary {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState>

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    fun startRanging(
        deviceConfigInterceptor: DeviceConfigInterceptor,
        phoneConfigInterceptor: PhoneConfigInterceptor,
        rangingConfig: RangingConfig = RangingConfig()
    ): Flow<RangingResult>
}

class GcxUwbBleLibrary(
    context: Context,
    uuidProvider: UUIDProvider = UUIDProvider()
) : UwbBleLibrary {

    private val bleManager: BleManager by lazy { GcxBleManager(context, uuidProvider) }
    private val bleScanner: BleScanner by lazy { GcxBleScanner(context) }
    private val uwbManager: UwbManager by lazy { UwbManager.createInstance(context) }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<GcxBleConnectionState> =
        bleManager.connect(bleDevice).map { it.toGcxBleConnectionState() }

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    override fun startRanging(
        deviceConfigInterceptor: DeviceConfigInterceptor,
        phoneConfigInterceptor: PhoneConfigInterceptor,
        rangingConfig: RangingConfig
    ): Flow<RangingResult> {
        val uwbControlee: UwbControlee = GcxUwbControlee(
            uwbManager = uwbManager,
            bleMessagingClient = bleManager.bleMessagingClient,
            deviceConfigInterceptor = deviceConfigInterceptor,
            phoneConfigInterceptor = phoneConfigInterceptor,
            rangingConfig = rangingConfig
        )
        return uwbControlee.startRanging()
    }
}
