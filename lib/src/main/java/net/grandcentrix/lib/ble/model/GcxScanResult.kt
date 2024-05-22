package net.grandcentrix.lib.ble.model

import android.Manifest
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.gatt.GattClient
import net.grandcentrix.lib.ble.gatt.GcxGattClient
import net.grandcentrix.lib.ble.provider.UUIDProvider

/**
 * Represents the result of a bluetooth scan.
 *
 * This class encapsulates a BLE scan result and provides functionality to connect to the discovered device.
 *
 * @property androidScanResult The raw BLE scan result obtained from the scanning process.
 */
data class GcxScanResult(
    val androidScanResult: ScanResult,
    private val context: Context
) {

    /**
     * Initiates a connection to the bluetooth device discovered during the scan.
     *
     * This function requires the `BLUETOOTH_CONNECT` permission to operate. It creates a bluetooth GATT client
     * to manage the connection and returns a [Flow] that emits [ConnectionState] objects representing
     * the state of the connection.

     * @param uuidProvider An instance of [UUIDProvider] that supplies the UUIDs required for the GATT connection.
     * @return A [Flow] emitting [ConnectionState] objects representing the connection state.
     */
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
