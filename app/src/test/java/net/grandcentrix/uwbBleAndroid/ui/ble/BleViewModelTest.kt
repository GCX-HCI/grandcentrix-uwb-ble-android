package net.grandcentrix.uwbBleAndroid.ui.ble

import android.Manifest
import android.bluetooth.BluetoothDevice
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.lib.UwbBleLibrary
import net.grandcentrix.lib.ble.model.ConnectionState
import net.grandcentrix.lib.ble.model.GcxScanResult
import net.grandcentrix.lib.ble.model.GcxUwbDevice
import net.grandcentrix.lib.ble.provider.UUIDProvider
import net.grandcentrix.uwbBleAndroid.model.BleScanResult
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import net.grandcentrix.uwbBleAndroid.testx.CoroutineTestExtension
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class BleViewModelTest {

    private val bluetoothDeviceMock: BluetoothDevice = mockk {
        every { address } returns "00:60:37:90:E7:11" // hardcoded mac address for mobile knowledge dev kit
    }

    private val uuidProvider: UUIDProvider = mockk()

    private val gcxScanResultMock: GcxScanResult = mockk {
        every { connect(uuidProvider) } returns emptyFlow()
        every { androidScanResult.device } returns bluetoothDeviceMock
    }

    private val bleScanResult: BleScanResult = mockk {
        every { bluetoothDevice } returns gcxScanResultMock.androidScanResult.device
        every { connectionState } returns ConnectionState.Disconnected
        every { gcxScanResult } returns gcxScanResultMock
    }

    private val uwbBleLibrary: UwbBleLibrary = mockk {
        every { startScan() } returns flowOf(gcxScanResultMock)
    }

    private val permissionChecker: PermissionChecker = mockk {
        every { hasPermissions(any()) } returns true
    }

    private val navigator: Navigator = mockk(relaxUnitFun = true)

    private val gcxUwbDevice: GcxUwbDevice = mockk()

    @Test
    fun `Given a known ble device, when starting ble scan, then ble device is shown`() = runTest {
        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
        viewModel.onToggleScanClicked()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assert(viewState.scanResults.isNotEmpty())

        verify { uwbBleLibrary.startScan() }
    }

    @Test
    fun `Given an unknown ble device, when starting ble scan, then ble device is not shown`() =
        runTest {
            every { uwbBleLibrary.startScan() } returns flowOf()

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assert(viewState.scanResults.isEmpty())

            verify { uwbBleLibrary.startScan() }
        }

    @Test
    fun `Given a running ble device scan, when stopping ble scan, then ble scan is stopped`() =
        runTest {
            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
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
            gcxScanResultMock.connect(uuidProvider)
        } returns flowOf(ConnectionState.Connected)

        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
        viewModel.onToggleScanClicked()
        viewModel.onDeviceClicked(bleScanResult)

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            ConnectionState.Connected,
            viewState.selectedScanResult?.connectionState
        )

        verify { gcxScanResultMock.connect(uuidProvider) }
    }

    @Test
    fun `Given an ble device, when discover services, then connection state is Services_Discovered`() =
        runTest {
            every {
                gcxScanResultMock.connect(uuidProvider)
            } returns flowOf(ConnectionState.ServicesDiscovered(gcxUwbDevice = gcxUwbDevice))

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onToggleScanClicked()
            viewModel.onDeviceClicked(bleScanResult)

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assertEquals(
                ConnectionState.ServicesDiscovered(gcxUwbDevice = gcxUwbDevice),
                viewState.selectedScanResult?.connectionState
            )
        }

    @Test
    fun `Given start scan, when found ble device, then ble device is disconnected`() = runTest {
        every {
            gcxScanResultMock.connect(uuidProvider)
        } returns flowOf(ConnectionState.Connected)

        val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
        viewModel.onToggleScanClicked()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            ConnectionState.Disconnected,
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

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            verify(exactly = 0) { uwbBleLibrary.startScan() }
        }

    @Test
    fun `Given scan permission is granted, when starting ble scan, then bleScanner startScan is called`() =
        runTest {
            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
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

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onDeviceClicked(bleScanResult)

            advanceUntilIdle()

            verify(exactly = 0) { gcxScanResultMock.connect(uuidProvider) }
        }

    @Test
    fun `Given connect permission is granted, when connect to device, then blemanager connect is called`() =
        runTest {
            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onDeviceClicked(bleScanResult)

            advanceUntilIdle()

            verify(exactly = 1) { gcxScanResultMock.connect(uuidProvider) }
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

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)
            viewModel.onToggleScanClicked()

            advanceUntilIdle()

            verify(exactly = 0) { uwbBleLibrary.startScan() }

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

            verify(exactly = 1) { uwbBleLibrary.startScan() }
        }

    @Test
    fun `Given connection to device succeeded, when user starts ranging, then navigate to RANGNING`() =
        runTest {
            every {
                gcxScanResultMock.connect(uuidProvider)
            } returns flowOf(ConnectionState.ServicesDiscovered(gcxUwbDevice = gcxUwbDevice))

            val viewModel = BleViewModel(uwbBleLibrary, permissionChecker, navigator, uuidProvider)

            viewModel.onDeviceClicked(bleScanResult)

            advanceUntilIdle()

            viewModel.onStartRangingClicked(gcxUwbDevice)

            verify {
                navigator.navigateTo(Screen.Ranging(gcxUwbDevice))
            }
        }
}
