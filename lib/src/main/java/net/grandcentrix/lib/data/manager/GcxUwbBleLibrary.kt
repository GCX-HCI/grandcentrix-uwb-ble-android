package net.grandcentrix.lib.data.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.manager.BleManager
import net.grandcentrix.lib.ble.manager.GcxBleManager
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.provider.UUIDProvider
import net.grandcentrix.lib.ble.scanner.BleScanner
import net.grandcentrix.lib.ble.scanner.GcxBleScanner
import net.grandcentrix.lib.logging.DefaultLogConfig
import net.grandcentrix.lib.logging.LogConfig
import net.grandcentrix.lib.logging.internal.GcxLogger

interface UwbBleLibrary {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState>
}

class GcxUwbBleLibrary(
    context: Context,
    uuidProvider: UUIDProvider = UUIDProvider(),
    logConfig: LogConfig = DefaultLogConfig()
) : UwbBleLibrary {

    private val logger: GcxLogger by lazy { GcxLogger.initialize(logConfig) }
    private val bleManager: BleManager by lazy { GcxBleManager(context, uuidProvider, logger) }
    private val bleScanner: BleScanner by lazy { GcxBleScanner(context, logger) }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun startScan(): Flow<ScanResult> = bleScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState> =
        bleManager.connect(bleDevice)
}
