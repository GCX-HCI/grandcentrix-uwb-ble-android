package net.grandcentrix.ble.manager

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.grandcentrix.ble.exception.BluetoothException
import net.grandcentrix.ble.model.BluetoothResult
import net.grandcentrix.ble.protocol.OOBMessageProtocol

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
}

class GcxBleManager(
    coroutineContext: CoroutineContext = Dispatchers.IO,
    private val context: Context,
    private val serviceUUID: UUID = UUID.fromString(UART_SERVICE),
    private val rxUUID: UUID = UUID.fromString(UART_RX_CHARACTERISTIC),
    private val txUUID: UUID = UUID.fromString(UART_TX_CHARACTERISTIC)
) : BleManager {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())
    private val bluetoothAdapter: BluetoothAdapter

    private val resultChannel = Channel<BluetoothResult>(Channel.CONFLATED)

    private var rxCharacteristic: BluetoothGattCharacteristic? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null

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
                    if (isRequiredServiceSupported(gatt)) {
                        observeTxCharacteristic(gatt)
                            .onFailure {
                                close(it)
                                cleanUpGattStack(gatt)
                            }

                        scope.launch {
                            writeRxCharacteristic(
                                gatt = gatt,
                                data = byteArrayOf(OOBMessageProtocol.INITIALIZE.command)
                            ).onFailure {
                                close(it)
                                cleanUpGattStack(gatt)
                            }
                        }
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
                resultChannel.trySend(
                    BluetoothResult(
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
                resultChannel.trySend(
                    BluetoothResult(
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
                super.onCharacteristicChanged(gatt, characteristic, value)
                when (value.first()) {
                    OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command -> Log.d(
                        TAG,
                        "onCharacteristicChanged: device config package ${value.contentToString()}"
                    )
                }
            }
        }
        try {
            val gatt = bleDevice.connectGatt(context, false, gattCallback)

            awaitClose {
                cleanUpGattStack(gatt)
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun writeRxCharacteristic(
        gatt: BluetoothGatt,
        data: ByteArray
    ): Result<BluetoothResult> = runCatching {
        val characteristic = checkNotNull(rxCharacteristic)
        gatt.writeCharacteristic(
            characteristic,
            data,
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        )
        waitForResult(characteristic.uuid)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun observeTxCharacteristic(gatt: BluetoothGatt): Result<Boolean> = runCatching {
        val characteristic = checkNotNull(txCharacteristic)
        gatt.setCharacteristicNotification(characteristic, true)
    }

    private suspend fun waitForResult(uuid: UUID): BluetoothResult {
        return withTimeoutOrNull(TimeUnit.SECONDS.toMillis(BLE_READ_WRITE_TIMEOUT)) {
            resultChannel.receiveAsFlow()
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
