package net.grandcentrix.lib.ble.model

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.manager.BleManager
import net.grandcentrix.lib.ble.manager.GcxBleManager
import net.grandcentrix.lib.ble.provider.UUIDProvider

data class GcxScanResult(
    val androidScanResult: ScanResult,
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(uuidProvider: UUIDProvider): Flow<ConnectionState> {
        val bleManager: BleManager = GcxBleManager(
            context = context,
            uuidProvider = uuidProvider
        )
        return bleManager.connect(bleDevice = androidScanResult.device)
    }
}

@RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
internal fun ScanResult.toGcxScanResult(context: Context): GcxScanResult = GcxScanResult(
    androidScanResult = this,
    context = context
)
