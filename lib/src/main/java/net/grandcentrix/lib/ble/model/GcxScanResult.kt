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
 * @property address The hardware address of a bluetooth deice.
 */
data class GcxScanResult(
    val address: String,
    private val context: Context
) {

    /**
     * Initiates a connection to the bluetooth device discovered during the scan.
     *
     * This function creates a bluetooth GATT client to manage the connection and returns
     * a [Flow] that emits [ConnectionState] objects representing
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
        return gattClient.connect(address = address)
    }
}

internal fun ScanResult.toGcxScanResult(context: Context): GcxScanResult = GcxScanResult(
    address = device.address,
    context = context
)
