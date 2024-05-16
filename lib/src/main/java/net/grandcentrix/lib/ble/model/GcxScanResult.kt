package net.grandcentrix.api.ble.model

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.api.ble.manager.BleManager
import net.grandcentrix.api.ble.manager.GcxBleManager
import net.grandcentrix.api.ble.provider.UUIDProvider
import net.grandcentrix.api.logging.DefaultLogConfig
import net.grandcentrix.api.logging.internal.GcxLogger

data class GcxScanResult(
    val bluetoothDevice: BluetoothDevice,
    val macAddress: String,
    val deviceName: String?,
    val rssi: Int,
    val scanRecord: ScanRecord?,
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(uuidProvider: UUIDProvider): Flow<ConnectionState> {
        val bleManager: BleManager = GcxBleManager(
            context = context,
            uuidProvider = uuidProvider,
            logger = GcxLogger.initialize(DefaultLogConfig()) // TODO
        )
        return bleManager.connect(bleDevice = bluetoothDevice)
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
internal fun ScanResult.toGcxScanResult(context: Context): GcxScanResult = GcxScanResult(
    bluetoothDevice = device,
    macAddress = device.address,
    deviceName = device.name,
    rssi = rssi,
    scanRecord = scanRecord,
    context = context
)
