package net.grandcentrix.ble.manager

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import net.grandcentrix.ble.exception.BluetoothException
import net.grandcentrix.ble.model.BluetoothMessage

private const val BLE_READ_WRITE_TIMEOUT: Long = 3
private const val TAG = "BleManager"

enum class ConnectionState {
    CONNECTED,
    DISCONNECTED,
    SERVICES_DISCOVERED
}

interface BleManager {
    fun bluetoothAdapter(): BluetoothAdapter

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState>

    val bleMessages: SharedFlow<BluetoothMessage>

    val bleMessagingClient: BleMessagingClient
}

interface BleMessagingClient {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun send(data: ByteArray): Result<BluetoothMessage>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableReceiver(): Result<Boolean>
}

class GcxBleManager(
    private val context: Context,
    private val serviceUUID: UUID = UUID.fromString(UART_SERVICE),
    private val rxUUID: UUID = UUID.fromString(UART_RX_CHARACTERISTIC),
    private val txUUID: UUID = UUID.fromString(UART_TX_CHARACTERISTIC)
) : BleManager {

    private val bluetoothAdapter: BluetoothAdapter
    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

    private var gatt: BluetoothGatt? = null

    private val _bleMessages =
        MutableSharedFlow<BluetoothMessage>(
            replay = 1
        )
    override val bleMessages = _bleMessages.asSharedFlow()

    override val bleMessagingClient: BleMessagingClient = object : BleMessagingClient {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override suspend fun send(data: ByteArray): Result<BluetoothMessage> = runCatching {
            val characteristic = checkNotNull(rxCharacteristic)
            val gatt = checkNotNull(gatt)
            gatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
            waitForResult(characteristic.uuid)
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun enableReceiver(): Result<Boolean> = runCatching {
            val characteristic = checkNotNull(txCharacteristic)
            val gatt = checkNotNull(gatt)
            gatt.setCharacteristicNotification(characteristic, true)
        }
    }
    override fun bluetoothAdapter(): BluetoothAdapter = bluetoothAdapter

    override fun connect(bleDevice: BluetoothDevice): Flow<ConnectionState> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    trySend(ConnectionState.CONNECTED)
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    trySend(ConnectionState.DISCONNECTED)
                    close()
                    gatt.close()
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
                _bleMessages.tryEmit(
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
                _bleMessages.tryEmit(
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
                _bleMessages.tryEmit(
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

            awaitClose {
                gatt?.let {
                    cleanUpGattStack(it)
                }
            }
        } catch (exception: SecurityException) {
            close(exception)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun cleanUpGattStack(gatt: BluetoothGatt) {
        gatt.disconnect()
        gatt.close()
    }

    private fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(serviceUUID)

        if (service != null) {
            rxCharacteristic = service.getCharacteristic(rxUUID)
            txCharacteristic = service.getCharacteristic(txUUID)
        }

        return rxCharacteristic != null && txCharacteristic != null
    }

    private suspend fun waitForResult(uuid: UUID): BluetoothMessage {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(BLE_READ_WRITE_TIMEOUT)) {
            bleMessages
                .filter { it.uuid == uuid }
                .first()
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
