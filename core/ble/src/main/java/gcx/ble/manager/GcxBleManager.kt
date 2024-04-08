package gcx.ble.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import gcx.ble.exception.BluetoothException
import gcx.ble.model.BluetoothResult
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val BLE_READ_WRITE_TIMEOUT: Long = 3
private const val TAG = "BleManager"

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
    coroutineContext: CoroutineContext = Dispatchers.IO,
    private val context: Context,
    private val serviceUUID: UUID = UUID.fromString(UART_SERVICE),
    private val rxUUID: UUID = UUID.fromString(UART_RX_CHARACTERISTIC),
    private val txUUID: UUID = UUID.fromString(UART_TX_CHARACTERISTIC),
) : BleManager {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())
    private val bluetoothAdapter: BluetoothAdapter

    private val resultChannel = Channel<BluetoothResult>()

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

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
                            if (isRequiredServiceSupported(gatt)) {
                                initialize(
                                    gatt = gatt
                                )
                            } else {
                                close(BluetoothException.ServiceNotSupportedException)
                            }
                        } else {
                            close(BluetoothException.ServiceDiscoveryFailedException)
                        }
                    }

                    override fun onCharacteristicRead(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray,
                        status: Int
                    ) {
                        resultChannel.trySend(
                            BluetoothResult(
                                uuid = characteristic.uuid,
                                data = value,
                                status = status,
                            )
                        )
                    }

                    override fun onCharacteristicWrite(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        status: Int
                    ) {
                        resultChannel.trySend(
                            BluetoothResult(
                                uuid = characteristic.uuid,
                                data = null,
                                status = status,
                            )
                        )
                    }

                    override fun onCharacteristicChanged(
                        gatt: BluetoothGatt,
                        characteristic: BluetoothGattCharacteristic,
                        value: ByteArray
                    ) {
                        super.onCharacteristicChanged(gatt, characteristic, value)
                        if (value.first() == 0x01.toByte()) {
                            Log.d(
                                TAG,
                                "onCharacteristicChanged: device config package ${value.contentToString()}"
                            )
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
                    gatt.disconnect()
                    gatt.close()
                }
            } catch (exception: SecurityException) {
                close(exception)
            }


        }

    private fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(serviceUUID)

        if (service != null) {
            rxCharacteristic = service.getCharacteristic(rxUUID)
            txCharacteristic = service.getCharacteristic(txUUID)
        }

        return rxCharacteristic != null && txCharacteristic != null
    }

    private fun initialize(gatt: BluetoothGatt) {
        observeTxCharacteristic(gatt)

        scope.launch {
            val writeResult = writeRxCharacteristic(
                gatt = gatt,
                data = byteArrayOf(0xA5.toByte())
            )
            Log.d(TAG, "write characteristic $writeResult")
        }
    }

    private suspend fun writeRxCharacteristic(
        gatt: BluetoothGatt,
        data: ByteArray
    ): BluetoothResult {
        rxCharacteristic?.let { characteristic ->
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
            return waitForResult(characteristic.uuid)
        } ?: run {
            throw BluetoothException.BluetoothNullPointerException("RX Characteristic")
        }
    }

    private fun observeTxCharacteristic(gatt: BluetoothGatt) {
        val characteristic = txCharacteristic
        gatt.setCharacteristicNotification(characteristic, true)
    }

    private suspend fun waitForResult(uuid: UUID): BluetoothResult {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(BLE_READ_WRITE_TIMEOUT)) {
            var bluetoothResult: BluetoothResult = resultChannel.receive()
            while (bluetoothResult.uuid != uuid) {
                bluetoothResult = resultChannel.receive()
            }
            bluetoothResult
        } ?: run {
            throw BluetoothException.BluetoothTimeoutException
        }
    }

    init {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
    }

    companion object {
        const val UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        const val UART_TX_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
        const val UART_RX_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
    }
}
