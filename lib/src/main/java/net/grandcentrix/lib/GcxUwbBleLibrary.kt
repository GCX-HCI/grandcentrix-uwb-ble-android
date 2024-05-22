package net.grandcentrix.lib

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.model.GcxScanResult
import net.grandcentrix.lib.ble.scanner.BleScanner
import net.grandcentrix.lib.ble.scanner.GcxBleScanner
import net.grandcentrix.lib.logging.DefaultLogConfig
import net.grandcentrix.lib.logging.LogConfig
import net.grandcentrix.lib.logging.internal.GcxLogger

interface UwbBleLibrary {

    /**
     * Starts a scanning process for bluetooth devices.
     *
     * This function requires both `ACCESS_FINE_LOCATION` and `BLUETOOTH_SCAN` permissions to operate.
     * It initiates a scan for nearby bluetooth devices and returns a [Flow] that emits [GcxScanResult] objects
     * as devices are discovered.

     * @return A [Flow] emitting [GcxScanResult] objects representing the devices discovered during the scan.
     */
    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN
        ]
    )
    fun startScan(): Flow<GcxScanResult>
}

class GcxUwbBleLibrary(
    context: Context,
    logConfig: LogConfig = DefaultLogConfig()
) : UwbBleLibrary {

    init {
        GcxLogger.configure(logConfig)
    }

    private val bleScanner: BleScanner by lazy { GcxBleScanner(context) }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN
        ]
    )
    override fun startScan(): Flow<GcxScanResult> = bleScanner.startScan()
}
