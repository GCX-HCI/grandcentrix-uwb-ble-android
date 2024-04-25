package net.grandcentrix.uwbBleAndroid

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.manager.ConnectionState
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.test.CoroutineTestExtension
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class MainActivityViewModelTest {

    private val bluetoothDevice: BluetoothDevice = mockk {
        every { address } returns "00:60:37:90:E7:11" // hardcoded mac address for mobile knowledge dev kit
    }
    private val scanResult: ScanResult = mockk {
        every { device } returns bluetoothDevice
    }
    private val bleScanner: BleScanner = mockk {
        every { startScan() } returns flowOf(scanResult)
    }

    private val bleManager: BleManager = mockk {
        every { connect(bluetoothDevice) } returns flow { }
    }

    private val permissionChecker: PermissionChecker = mockk {
        every {
            hasPermissions(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        } returns true

        every { hasPermission(Manifest.permission.BLUETOOTH_CONNECT) } returns true

        every { hasPermission(Manifest.permission.BLUETOOTH_SCAN) } returns true

        every { hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) } returns true
    }

    @Test
    fun `Given a known ble device, when starting ble scan, then ble device is shown`() = runTest {
        val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
        viewModel.scan()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assert(viewState.results.isNotEmpty())
    }

    @Test
    fun `Given an unknown ble device, when starting ble scan, then ble device is not shown`() =
        runTest {
            every { bleScanner.startScan() } returns flowOf()

            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.scan()

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assert(viewState.results.isEmpty())
        }

    @Test
    fun `Given an ble device, when connect to device, then ble device is connected`() = runTest {
        every { bleManager.connect(bluetoothDevice) } returns flowOf(ConnectionState.CONNECTED)

        val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
        viewModel.scan()
        viewModel.connectToDevice(bluetoothDevice)

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            ConnectionState.CONNECTED,
            viewState.results.first().connectionState
        )
    }

    @Test
    fun `Given an ble device, when discover services, then connection state is Services_Discovered`() =
        runTest {
            every {
                bleManager.connect(bluetoothDevice)
            } returns flowOf(ConnectionState.SERVICES_DISCOVERED)

            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.scan()
            viewModel.connectToDevice(bluetoothDevice)

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assertEquals(
                ConnectionState.SERVICES_DISCOVERED,
                viewState.results.first().connectionState
            )
        }

    @Test
    fun `Given start scan, when found ble device, then ble device is disconnected`() = runTest {
        every { bleManager.connect(bluetoothDevice) } returns flowOf(ConnectionState.CONNECTED)

        val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
        viewModel.scan()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            ConnectionState.DISCONNECTED,
            viewState.results.first().connectionState
        )
    }

    @Test
    fun `Given scan permission not granted, when starting ble scan, then bleScanner startScan is not called`() =
        runTest {
            every {
                permissionChecker.hasPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            } returns false

            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.scan()

            advanceUntilIdle()

            verify(exactly = 0) { bleScanner.startScan() }
        }

    @Test
    fun `Given scan permission is granted, when starting ble scan, then bleScanner startScan is called`() =
        runTest {
            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.scan()

            advanceUntilIdle()

            verify(exactly = 1) { bleScanner.startScan() }
        }

    @Test
    fun `Given connect permission not granted, when connect to device, then blemanager connect is not called`() =
        runTest {
            every {
                permissionChecker.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
            } returns false

            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.connectToDevice(bluetoothDevice)

            advanceUntilIdle()

            verify(exactly = 0) { bleManager.connect(bluetoothDevice) }
        }

    @Test
    fun `Given connect permission is granted, when connect to device, then blemanager connect is called`() =
        runTest {
            val viewModel = MainActivityViewModel(bleScanner, bleManager, permissionChecker)
            viewModel.connectToDevice(bluetoothDevice)

            advanceUntilIdle()

            verify(exactly = 1) { bleManager.connect(bluetoothDevice) }
        }
}
