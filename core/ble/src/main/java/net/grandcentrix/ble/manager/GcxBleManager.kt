package net.grandcentrix.ble.manager

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import net.grandcentrix.ble.exception.BluetoothException
import net.grandcentrix.ble.model.BluetoothMessage
import net.grandcentrix.ble.provider.UUIDProvider

private const val BLE_READ_WRITE_TIMEOUT: Long = 3
private const val TAG = "BleManager"

enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    SERVICES_DISCOVERED
}

interface BleManager {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState>

    val bleMessagingClient: BleMessagingClient
}

interface BleMessagingClient {
    val messages: Flow<BluetoothMessage>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun send(data: ByteArray): Result<Unit>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableReceiver(): Result<Boolean>
}

class GcxBleManager(
    private val context: Context,
    private val uuidProvider: UUIDProvider
) : BleManager {

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    private var gatt: BluetoothGatt? = null

    private val bleMessages = MutableSharedFlow<BluetoothMessage>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val bleMessagingClient: BleMessagingClient = object : BleMessagingClient {
        override val messages = bleMessages.asSharedFlow()

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override suspend fun send(data: ByteArray): Result<Unit> = runCatching {
            val characteristic = checkNotNull(rxCharacteristic)
            val gatt = checkNotNull(gatt)
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun enableReceiver(): Result<Boolean> = runCatching {
            val characteristic = checkNotNull(txCharacteristic)
            val gatt = checkNotNull(gatt)
            gatt.setCharacteristicNotification(characteristic, true)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    trySend(ConnectionState.CONNECTED)
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    trySend(ConnectionState.DISCONNECTED)
                    gatt.close()
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        close()
                    } else {
                        close(BluetoothException.ConnectionFailure(status))
                    }
                }
            }

            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    trySend(ConnectionState.SERVICES_DISCOVERED)
                    if (!isRequiredServiceSupported(gatt)) {
                        close(BluetoothException.ServiceNotSupportedException)
                        cleanUpGattStack(gatt)
                    }
                } else {
                    close(BluetoothException.ServiceDiscoveryFailedException)
                    cleanUpGattStack(gatt)
                }
            }

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                bleMessages.tryEmit(
                    BluetoothMessage(
                        uuid = characteristic.uuid,
                        data = value,
                        status = status
                    )
                )
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.d(
                    TAG,
                    "onCharacteristicWrite ->\n" +
                        "uuid: ${characteristic.uuid}\n" +
                        "status: $status"
                )
                bleMessages.tryEmit(
                    BluetoothMessage(
                        uuid = characteristic.uuid,
                        data = null,
                        status = status
                    )
                )
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                Log.d(
                    TAG,
                    "onCharacteristicChanged ->\n" +
                        "uuid: ${characteristic.uuid}\n" +
                        "value: ${value.contentToString()}"
                )
                bleMessages.tryEmit(
                    BluetoothMessage(
                        uuid = characteristic.uuid,
                        data = value,
                        status = BluetoothGatt.GATT_SUCCESS
                    )
                )
            }
        }
        try {
            gatt = bleDevice.connectGatt(context, false, gattCallback)
        } catch (exception: SecurityException) {
            close(exception)
        }

        awaitClose {
            gatt?.let { cleanUpGattStack(it) }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cleanUpGattStack(gatt: BluetoothGatt) {
        gatt.disconnect()
        gatt.close()
    }

    private fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(uuidProvider.serviceUUID)

        if (service != null) {
            rxCharacteristic = service.getCharacteristic(uuidProvider.rxUUID)
            txCharacteristic = service.getCharacteristic(uuidProvider.txUUID)
        }

        return rxCharacteristic != null && txCharacteristic != null
    }

    companion object {
        const val UART_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
        const val UART_TX_CHARACTERISTIC = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
        const val UART_RX_CHARACTERISTIC = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
    }
}
