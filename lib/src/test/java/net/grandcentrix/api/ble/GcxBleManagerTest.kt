package net.grandcentrix.api.ble

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
import net.grandcentrix.api.ble.exception.BluetoothException
import net.grandcentrix.api.ble.manager.ConnectionState
import net.grandcentrix.api.ble.manager.GcxBleManager
import net.grandcentrix.api.ble.provider.UUIDProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GcxBleManagerTest {

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
    private val bluetoothAdapter: BluetoothAdapter = mockk()
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
    fun `Given bluetooth device, when connect to gatt success, then return connection state CONNECTED`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice).collect { connectionState ->
                    assertEquals(ConnectionState.CONNECTED, connectionState)
                }
            }
            advanceUntilIdle()
        }

    @Test
    fun `Given bluetooth device, when connection attempt fails, then return connection state DISCONNECTED`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice).collect { connectionState ->
                    assertEquals(ConnectionState.DISCONNECTED, connectionState)
                }
            }
            advanceUntilIdle()
        }

    @Test
    fun `Given bluetooth device, when connect to gatt success, then discover services is called`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice).collect()
            }
            advanceUntilIdle()
            verify {
                bluetoothGatt.discoverServices()
            }
        }

    @Test
    fun `Given bluetooth device, when service discovered success, then return connection state SERVICES_DISCOVERED`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice).collect { connectionState ->
                    assertEquals(ConnectionState.SERVICES_DISCOVERED, connectionState)
                }
            }
            advanceUntilIdle()
        }

    @Test
    fun `Given bluetooth device, when service discovered failed, then throw ServiceDiscoveryFailedException`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice)
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.ServiceDiscoveryFailedException, thrownError)
        }

    @Test
    fun `Given bluetooth device, when services not supported, then throw ServiceNotSupportedException`() =
        runTest {
            val bluetoothDevice: BluetoothDevice = mockk()
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

            val gxcBleManager = GcxBleManager(context = context, uuidProvider = uuidProvider)

            var thrownError: Throwable? = null
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gxcBleManager.connect(bluetoothDevice)
                    .catch { thrownError = it }
                    .collect()
            }
            advanceUntilIdle()
            assertEquals(BluetoothException.ServiceNotSupportedException, thrownError)
        }
}
