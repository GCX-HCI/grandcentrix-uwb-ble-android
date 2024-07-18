package net.grandcentrix.lib.ble.gatt

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import net.grandcentrix.lib.ble.exception.BluetoothException
import net.grandcentrix.lib.ble.model.BluetoothMessage
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.model.GcxUwbDevice
import net.grandcentrix.lib.ble.provider.UUIDProvider
import net.grandcentrix.lib.logging.internal.GcxLogger

private const val BLE_READ_WRITE_TIMEOUT: Long = 3
private const val TAG = "GcxGattClient"

interface GattClient {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun connect(address: String): Flow<ConnectionState>

    val bleMessagingClient: BleMessagingClient
}

interface BleMessagingClient {
    val messages: Flow<BluetoothMessage>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun send(data: ByteArray): Result<Unit>

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun enableReceiver(): Result<Boolean>
}

internal class GcxGattClient(
    private val context: Context,
    private val uuidProvider: UUIDProvider
) : GattClient {

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

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
    override fun connect(address: String): Flow<ConnectionState> = callbackFlow {
        val gattCallback = object : BluetoothGattCallback() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    trySend(ConnectionState.Connected)
                    gatt.discoverServices()
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    trySend(ConnectionState.Disconnected)
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
                    if (isRequiredServiceSupported(gatt)) {
                        trySend(
                            ConnectionState.ServicesDiscovered(
                                gcxUwbDevice = GcxUwbDevice(
                                    context = context,
                                    bleMessagingClient = bleMessagingClient
                                )
                            )
                        )
                    } else {
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
                GcxLogger.v(
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
                GcxLogger.v(
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
            getBluetoothDeviceByAddress(address = address)
                .onFailure { close(BluetoothException.BluetoothMacAddressInvalidException) }
                .onSuccess { gatt = it.connectGatt(context, false, gattCallback) }
        } catch (exception: SecurityException) {
            close(exception)
        }

        awaitClose {
            gatt?.let { cleanUpGattStack(it) }
        }
    }

    private fun getBluetoothDeviceByAddress(address: String): Result<BluetoothDevice> =
        runCatching {
            bluetoothAdapter.getRemoteDevice(address)
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
