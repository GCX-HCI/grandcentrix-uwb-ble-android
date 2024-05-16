package net.grandcentrix.lib.data.manager

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

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
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
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        ]
    )
    override fun startScan(): Flow<GcxScanResult> = bleScanner.startScan()
}
