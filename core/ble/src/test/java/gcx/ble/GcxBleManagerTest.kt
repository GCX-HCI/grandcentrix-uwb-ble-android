package gcx.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import gcx.ble.exception.BluetoothException
import gcx.ble.manager.ConnectionState
import gcx.ble.manager.GcxBleManager
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GcxBleManagerTest {

    private val bluetoothGatt: BluetoothGatt = mockk {
        justRun { disconnect() }
        justRun { close() }
        every { discoverServices() } returns true
    }
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val bluetoothManager: BluetoothManager = mockk()
    private val context: Context = mockk {
        every { getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter
    }

    @Test
    fun `Given bluetooth device, when connect to gatt success, then return connection state CONNECTED`() = runTest {
        val bluetoothDevice: BluetoothDevice = mockk()
        val gattCallbackCapture = slot<BluetoothGattCallback>()

        every { bluetoothDevice.connectGatt(any(), any(), capture(gattCallbackCapture)) } answers {
            gattCallbackCapture.captured.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
            bluetoothGatt
        }

        val gxcBleManager = GcxBleManager(context = context)


        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gxcBleManager.connect(bluetoothDevice).collect {connectionState ->
                println("collect ${connectionState.name}")
                assertEquals(ConnectionState.CONNECTED, connectionState)
            }
        }
      advanceUntilIdle()
    }

    @Test
    fun `Given bluetooth device, when flow callback is canceled, then return connection state DISCONNECTED`() = runTest {
        val bluetoothDevice: BluetoothDevice = mockk()
        val gattCallbackCapture = slot<BluetoothGattCallback>()

        every { bluetoothDevice.connectGatt(any(), any(), capture(gattCallbackCapture)) } answers {
            gattCallbackCapture.captured.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED)
            bluetoothGatt
        }

        val gxcBleManager = GcxBleManager(context = context)


        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gxcBleManager.connect(bluetoothDevice).collect {connectionState ->
                println("collect ${connectionState.name}")
                assertEquals(ConnectionState.DISCONNECTED, connectionState)
            }
        }
        advanceUntilIdle()
    }

    @Test
    fun `Given bluetooth device, when connect to gatt success, then discover services is called`() = runTest {
        val bluetoothDevice: BluetoothDevice = mockk()
        val gattCallbackCapture = slot<BluetoothGattCallback>()

        every { bluetoothDevice.connectGatt(any(), any(), capture(gattCallbackCapture)) } answers {
            gattCallbackCapture.captured.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
            bluetoothGatt
        }
        val gxcBleManager = GcxBleManager(context = context)


        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gxcBleManager.connect(bluetoothDevice).collect()
        }
        advanceUntilIdle()
        verify {
            bluetoothGatt.discoverServices()
        }
    }

    @Test
    fun `Given bluetooth device, when service discovered success, then return connection state READY`() = runTest {
        val bluetoothDevice: BluetoothDevice = mockk()
        val gattCallbackCapture = slot<BluetoothGattCallback>()

        every { bluetoothDevice.connectGatt(any(), any(), capture(gattCallbackCapture)) } answers {
            gattCallbackCapture.captured.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS)
            bluetoothGatt
        }

        val gxcBleManager = GcxBleManager(context = context)


        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gxcBleManager.connect(bluetoothDevice).collect {connectionState ->
                println("collect ${connectionState.name}")
                assertEquals(ConnectionState.SERVICES_DISCOVERED, connectionState)
            }
        }
        advanceUntilIdle()
    }

    @Test
    fun `Given bluetooth device, when service discovered failed, then throw ServiceDiscoveryFailedException`() = runTest {
        val bluetoothDevice: BluetoothDevice = mockk()
        val gattCallbackCapture = slot<BluetoothGattCallback>()

        every { bluetoothDevice.connectGatt(any(), any(), capture(gattCallbackCapture)) } answers {
            gattCallbackCapture.captured.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_FAILURE)
            bluetoothGatt
        }

        val gxcBleManager = GcxBleManager(context = context)


        var thrownError: Throwable? = null
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gxcBleManager.connect(bluetoothDevice)
                .catch { thrownError = it }
                .collect()
        }
        advanceUntilIdle()
        assertEquals(BluetoothException.ServiceDiscoveryFailedException, thrownError)
    }
}