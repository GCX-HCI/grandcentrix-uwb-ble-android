package net.grandcentrix.lib.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.grandcentrix.lib.ble.exception.BluetoothException
import net.grandcentrix.lib.ble.model.GcxScanResult
import net.grandcentrix.lib.ble.model.toGcxScanResult
import net.grandcentrix.lib.logging.internal.GcxLogger

private const val TAG = "BleScanner"

interface BleScanner {

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN
        ]
    )
    fun startScan(): Flow<GcxScanResult>
}

internal class GcxBleScanner(private val context: Context) : BleScanner {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    override fun startScan(): Flow<GcxScanResult> = callbackFlow {
        if (!bluetoothAdapter.isEnabled) {
            close(BluetoothException.BluetoothDisabledException)
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                GcxLogger.v(TAG, "Found ble device: ${result.device}")
                trySend(result.toGcxScanResult(context = context))
                    .onFailure { GcxLogger.d(TAG, "Failed to send scan result", it) }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                close(BluetoothException.ScanFailure(errorCode))
            }
        }

        try {
            bluetoothLeScanner.startScan(scanCallback)
        } catch (exception: SecurityException) {
            GcxLogger.e(TAG, "Failed to start scan", exception)
            close(exception)
        }

        awaitClose {
            try {
                bluetoothLeScanner.stopScan(scanCallback)
            } catch (exception: SecurityException) {
                GcxLogger.e(TAG, "Failed to stop scan", exception)
            }
        }
    }
}
