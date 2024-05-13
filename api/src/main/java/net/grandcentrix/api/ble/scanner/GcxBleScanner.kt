package net.grandcentrix.ble.scanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import net.grandcentrix.ble.exception.BluetoothException

private const val TAG = "BleScanner"

interface BleScanner {

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun startScan(): Flow<ScanResult>
}

class GcxBleScanner(
    context: Context
) : BleScanner {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    override fun startScan(): Flow<ScanResult> = callbackFlow {
        if (!bluetoothAdapter.isEnabled) {
            close(BluetoothException.BluetoothDisabledException)
            return@callbackFlow
        }

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                trySend(result)
                    .onFailure { Log.d(TAG, "Failed to send scan result", it) }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                close(BluetoothException.ScanFailure(errorCode))
            }
        }

        try {
            bluetoothLeScanner.startScan(scanCallback)
        } catch (exception: SecurityException) {
            Log.e(TAG, "Failed to start scan", exception)
            close(exception)
        }

        awaitClose {
            try {
                bluetoothLeScanner.stopScan(scanCallback)
            } catch (exception: SecurityException) {
                Log.e(TAG, "Failed to stop scan", exception)
            }
        }
    }
}
