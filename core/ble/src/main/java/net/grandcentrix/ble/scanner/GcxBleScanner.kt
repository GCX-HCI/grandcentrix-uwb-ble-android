package net.grandcentrix.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.grandcentrix.ble.exception.BluetoothException
import net.grandcentrix.ble.manager.BleManager

private const val TAG = "BleScanner"

interface BleScanner {

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    fun startScan(): Flow<ScanResult>
}

class GcxBleScanner(
    bleManager: BleManager
) : BleScanner {
    private val bluetoothAdapter: BluetoothAdapter = bleManager.bluetoothAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    override fun startScan(): Flow<ScanResult> = callbackFlow {
        if (!bluetoothAdapter.isEnabled) {
            close(BluetoothException.BluetoothDisabledException)
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                trySend(result)
                    .onFailure {
                        Log.d(TAG, "trySend throws failure $it")
                    }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                close()
            }
        }

        try {
            bluetoothLeScanner.startScan(scanCallback)
        } catch (exception: SecurityException) {
            Log.e(TAG, "scan failed with $exception")
            close(exception)
        }

        awaitClose {
            try {
                bluetoothLeScanner.stopScan(scanCallback)
            } catch (exception: SecurityException) {
                Log.e(TAG, "stop scan failed", exception)
            }
        }
    }
}
