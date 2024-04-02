package bjoern.kinberger.gcx.ble.scanner

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import bjoern.kinberger.gcx.ble.manager.BleManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "BLE_SCANNER"

interface BleScanner {
    fun startScan(): Flow<ScanResult>
}

class GcxBleScanner(
    private val bleManager: BleManager,
) : BleScanner {
    private val bluetoothLeScanner: BluetoothLeScanner = bleManager.bluetoothAdapter().bluetoothLeScanner

    override fun startScan(): Flow<ScanResult> =
        callbackFlow {
            val scanCallback =
                object : ScanCallback() {
                    override fun onScanResult(
                        callbackType: Int,
                        result: ScanResult,
                    ) {
                        super.onScanResult(callbackType, result)
                        trySend(result)
                            .onFailure {
                                Log.d(TAG, "trySend throws failure $it")
                            }
                    }
                }

            try {
                bluetoothLeScanner.startScan(scanCallback)
            } catch (exception: SecurityException) {
                Log.d(TAG, "scan failed with $exception")
            }

            awaitClose {
                try {
                    bluetoothLeScanner.stopScan(scanCallback)
                } catch (exception: SecurityException) {
                    Log.d(TAG, "stop scan failed with $exception")
                }
            }
        }
}
