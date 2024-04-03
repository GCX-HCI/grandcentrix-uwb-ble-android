package gcx.ble.scanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import gcx.ble.manager.BleManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "BLE_SCANNER"

interface BleScanner {
    fun startScan(onScanFailure: (Error) -> Unit): Flow<ScanResult>
}

class GcxBleScanner(
    private val bleManager: BleManager,
) : BleScanner {
    private val bluetoothAdapter: BluetoothAdapter = bleManager.bluetoothAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    override fun startScan(onScanFailure: (Error) -> Unit): Flow<ScanResult> =
        callbackFlow {
            if (!bluetoothAdapter.isEnabled) {
                onScanFailure(Error("BT Adapter is not turned on!"))
                close()
                return@callbackFlow
            }

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

                    override fun onScanFailed(errorCode: Int) {
                        super.onScanFailed(errorCode)
                        onScanFailure(Error("Scan failed with error code: $errorCode"))
                    }
                }

            try {
                bluetoothLeScanner.startScan(scanCallback)
            } catch (exception: SecurityException) {
                Log.d(TAG, "scan failed with $exception")
                onScanFailure(Error("Permission missing! $exception"))
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
