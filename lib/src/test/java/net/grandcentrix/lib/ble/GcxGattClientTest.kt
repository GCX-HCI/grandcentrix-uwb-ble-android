package net.grandcentrix.lib.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.lib.ble.exception.BluetoothException
import net.grandcentrix.lib.ble.gatt.GcxGattClient
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.provider.UUIDProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val VALID_MAC_ADRESS = "VALID_MAC_ADDRESS"

@OptIn(ExperimentalCoroutinesApi::class)
class GcxGattClientTest {

    private val txCharacteristic: BluetoothGattCharacteristic = mockk {
        every { uuid } returns UUID.randomUUID()
    }
    private val rxCharacteristic: BluetoothGattCharacteristic = mockk {
        every { uuid } returns UUID.randomUUID()
    }
    private val service: BluetoothGattService = mockk {
        every { getCharacteristic(any()) } returns txCharacteristic
        every { getCharacteristic(any()) } returns rxCharacteristic
    }
    private val bluetoothGatt: BluetoothGatt = mockk {
        every { getService(any()) } returns service
        every { setCharacteristicNotification(any(), any()) } returns true
        every { writeCharacteristic(any(), any(), any()) } returns 0
        justRun { disconnect() }
        justRun { close() }
        every { discoverServices() } returns true
    }

    val bluetoothDevice: BluetoothDevice = mockk {
        every { address } returns VALID_MAC_ADRESS
    }

    private val bluetoothAdapter: BluetoothAdapter = mockk {
        every { getRemoteDevice(VALID_MAC_ADRESS) } returns bluetoothDevice
    }
    private val bluetoothManager: BluetoothManager = mockk()
    private val context: Context = mockk {
        every {
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter
    }

    private val uuidProvider: UUIDProvider = mockk {
        every { serviceUUID } returns UUID.randomUUID()
        every { txUUID } returns UUID.randomUUID()
        every { rxUUID } returns UUID.randomUUID()
    }

    @Test
    fun `Given bluetooth adapter, when get remote device with invalid mac, then throw exception`() =
        runTest {
            justRun {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    any()
                )
            }

            every {
                bluetoothAdapter.getRemoteDevice(
                    "INVALID_MAC_ADDRESS"
                )
            } throws IllegalArgumentException()

            every {
                (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
            } returns bluetoothAdapter
            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect("INVALID_MAC_ADDRESS")
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.BluetoothMacAddressInvalidException, thrownError)
        }

    @Test
    fun `Given bluetooth device address, when connect to gatt success, then return connection state CONNECTED`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onConnectionStateChange(
                    bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS,
                    BluetoothProfile.STATE_CONNECTED
                )
                bluetoothGatt
            }

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS).collect { connectionState ->
                    assertEquals(ConnectionState.Connected, connectionState)
                }
            }
            advanceUntilIdle()
        }

    @Test
    fun `Given bluetooth device address, when connection attempt fails, then return connection state DISCONNECTED`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onConnectionStateChange(
                    bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS,
                    BluetoothProfile.STATE_DISCONNECTED
                )
                bluetoothGatt
            }

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS)
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.ServiceDiscoveryFailedException, thrownError)
        }

    @Test
    fun `Given bluetooth device address, when connect to gatt success, then discover services is called`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onConnectionStateChange(
                    bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS,
                    BluetoothProfile.STATE_CONNECTED
                )
                bluetoothGatt
            }

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS).collect()
            }
            advanceUntilIdle()
            verify {
                bluetoothGatt.discoverServices()
            }
        }

    @Test
    fun `Given bluetooth device address, when service discovered success, then return connection state SERVICES_DISCOVERED`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onServicesDiscovered(
                    bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS
                )
                bluetoothGatt
            }

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS).collect { connectionState ->
                    assert(connectionState is ConnectionState.ServicesDiscovered)
                }
            }
            advanceUntilIdle()
        }

    @Test
    fun `Given bluetooth device address, when service discovered failed, then throw ServiceDiscoveryFailedException`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onServicesDiscovered(
                    bluetoothGatt,
                    BluetoothGatt.GATT_FAILURE
                )
                bluetoothGatt
            }

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS)
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.ServiceDiscoveryFailedException, thrownError)
        }

    @Test
    fun `Given bluetooth device address, when services not supported, then throw ServiceNotSupportedException`() =
        runTest {
            val gattCallbackCapture = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    any(),
                    any(),
                    capture(gattCallbackCapture)
                )
            } answers {
                gattCallbackCapture.captured.onServicesDiscovered(
                    bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS
                )
                bluetoothGatt
            }

            every { service.getCharacteristic(any()) } returns null

            every { bluetoothGatt.discoverServices() } returns true

            val gcxGattClient = GcxGattClient(
                context = context,
                uuidProvider = uuidProvider
            )

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxGattClient.connect(VALID_MAC_ADRESS)
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.ServiceNotSupportedException, thrownError)
        }
}
