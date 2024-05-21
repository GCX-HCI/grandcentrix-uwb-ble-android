package net.grandcentrix.lib.ble.model

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.gatt.GattClient
import net.grandcentrix.lib.ble.gatt.GcxGattClient
import net.grandcentrix.lib.ble.provider.UUIDProvider

data class GcxScanResult(
    val androidScanResult: ScanResult,
    private val context: Context
) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(uuidProvider: UUIDProvider): Flow<ConnectionState> {
        val gattClient: GattClient = GcxGattClient(
            context = context,
            uuidProvider = uuidProvider
        )
        return gattClient.connect(bleDevice = androidScanResult.device)
    }
}
internal fun ScanResult.toGcxScanResult(context: Context): GcxScanResult = GcxScanResult(
    androidScanResult = this,
    context = context
)
