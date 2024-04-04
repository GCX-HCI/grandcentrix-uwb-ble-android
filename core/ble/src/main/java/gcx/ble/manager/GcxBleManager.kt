package gcx.ble.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "GcxBleManager"

enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    READY,
}

interface BleManager {
    fun bluetoothAdapter(): BluetoothAdapter

    fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState>
}

class GcxBleManager(
    private val context: Context,
) : BleManager {
    private val bluetoothAdapter: BluetoothAdapter

    override fun bluetoothAdapter(): BluetoothAdapter = bluetoothAdapter

    override fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState> =
        callbackFlow {
            val gattCallback =
                object : BluetoothGattCallback() {
                    override fun onConnectionStateChange(
                        gatt: BluetoothGatt,
                        status: Int,
                        newState: Int,
                    ) {
                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            trySend(ConnectionState.CONNECTED)
                            gatt.discoverServices()
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            trySend(ConnectionState.DISCONNECTED)
                            close()
                        }
                    }

                    override fun onServicesDiscovered(
                        gatt: BluetoothGatt,
                        status: Int,
                    ) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            trySend(ConnectionState.READY)
                        } else {
                            Log.e(TAG, "onServicesDiscovered: failed $status")
                            close()
                        }
                    }
                }
            try {
                val gatt =
                    bleDevice.connectGatt(
                        context,
                        false,
                        gattCallback,
                    )

                awaitClose {
                    gatt.close()
                }
            } catch (exception: SecurityException) {
                Log.e(TAG, "connect failed with", exception)
                close(exception)
            }


        }

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
    }
}
