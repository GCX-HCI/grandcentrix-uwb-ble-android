package net.grandcentrix.uwbBleAndroid.ui.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.data.manager.UwbBleLibrary
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.test.CoroutineTestExtension
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class BleViewModelTest {

    private val bluetoothDeviceMock: BluetoothDevice = mockk {
        every { address } returns "00:60:37:90:E7:11" // hardcoded mac address for mobile knowledge dev kit
    }
    private val bleDevice: GcxBleDevice = mockk {
        every { bluetoothDevice } returns bluetoothDeviceMock
    }
    private val scanResult: ScanResult = mockk {
        every { device } returns bluetoothDeviceMock
    }
    private val uwbBleLibrary: UwbBleLibrary = mockk {
        every { startScan() } returns flowOf(scanResult)
        every { connect(bluetoothDeviceMock) } returns flow { }
        coJustRun { startRanging() }
    }

    private val permissionChecker: PermissionChecker = mockk {
        every { hasPermissions(any()) } returns true
    }

    @Test
    fun `Given a known ble device, when starting ble scan, then ble device is shown`() = runTest {
        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
        viewModel.onToggleScanClicked()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assert(viewState.scanResults.isNotEmpty())

        verify { uwbBleManager.startScan() }
    }

    @Test
    fun `Given an unknown ble device, when starting ble scan, then ble device is not shown`() =
        runTest {
            every { uwbBleLibrary.startScan() } returns flowOf()

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assert(viewState.scanResults.isEmpty())

            verify { uwbBleManager.startScan() }
        }

    @Test
    fun `Given a running ble device scan, when stopping ble scan, then ble scan is stopped`() =
        runTest {
            val viewModel = BleViewModel(uwbBleManager, permissionChecker)
            // Given a running ble scan
            viewModel.onToggleScanClicked()
            advanceUntilIdle()
            assertEquals(true, viewModel.viewState.value.isScanning)

            // When stopping ble scan
            viewModel.onToggleScanClicked()
            advanceUntilIdle()

            // Then scan is not running
            assertEquals(false, viewModel.viewState.value.isScanning)

        }

    @Test
    fun `Given an ble device, when connect to device, then ble device is connected`() = runTest {
        every {
            uwbBleLibrary.connect(bluetoothDeviceMock)
        } returns flowOf(GcxBleConnectionState.CONNECTED)

        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
        viewModel.onToggleScanClicked()
        viewModel.onDeviceClicked(bleDevice)

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            GcxBleConnectionState.CONNECTED,
            viewState.scanResults.first().connectionState
        )

        verify { uwbBleManager.connect(bleDevice.bluetoothDevice) }
    }

    @Test
    fun `Given an ble device, when discover services, then connection state is Services_Discovered`() =
        runTest {
            every {
                uwbBleLibrary.connect(bluetoothDeviceMock)
            } returns flowOf(GcxBleConnectionState.SERVICES_DISCOVERED)

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onToggleScanClicked()
            viewModel.onDeviceClicked(bleDevice)

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assertEquals(
                GcxBleConnectionState.SERVICES_DISCOVERED,
                viewState.scanResults.first().connectionState
            )
        }

    @Test
    fun `Given start scan, when found ble device, then ble device is disconnected`() = runTest {
        every {
            uwbBleLibrary.connect(bluetoothDeviceMock)
        } returns flowOf(GcxBleConnectionState.CONNECTED)

        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
        viewModel.onToggleScanClicked()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            GcxBleConnectionState.DISCONNECTED,
            viewState.scanResults.first().connectionState
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

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            verify(exactly = 0) { uwbBleLibrary.startScan() }
        }

    @Test
    fun `Given scan permission is granted, when starting ble scan, then bleScanner startScan is called`() =
        runTest {
            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            verify(exactly = 1) { uwbBleLibrary.startScan() }
        }

    @Test
    fun `Given connect permission not granted, when connect to device, then blemanager connect is not called`() =
        runTest {
            every {
                permissionChecker.hasPermissions(listOf(Manifest.permission.BLUETOOTH_CONNECT))
            } returns false

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onDeviceClicked(bleDevice)

            advanceUntilIdle()

            verify(exactly = 0) { uwbBleLibrary.connect(bluetoothDeviceMock) }
        }

    @Test
    fun `Given connect permission is granted, when connect to device, then blemanager connect is called`() =
        runTest {
            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker)
            viewModel.onDeviceClicked(bleDevice)

            advanceUntilIdle()

            verify(exactly = 1) { uwbBleLibrary.connect(bluetoothDeviceMock) }
        }


    @Test
    fun `Given scan permission is not granted when starting to scan, when granting the permission, then scan should start right after`() =
        runTest {
            every {
                permissionChecker.hasPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            } returns false

            val viewModel = BleViewModel(uwbBleManager, permissionChecker)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            verify(exactly = 0) { uwbBleManager.startScan() }

            every {
                permissionChecker.hasPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            } returns true

            viewModel.onPermissionResult()

            advanceUntilIdle()

            verify(exactly = 1) { uwbBleManager.startScan() }
        }
}
