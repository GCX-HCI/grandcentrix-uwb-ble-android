package gcx.grandcentrix_uwb_ble_android

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import gcx.ble.manager.BleManager
import gcx.ble.manager.ConnectionState
import gcx.ble.scanner.BleScanner
import gcx.test.CoroutineTestExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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

    private val bleManager: BleManager = mockk()

    @Test
    fun `Given a known ble device, when starting ble scan, then ble device is shown`() = runTest {
        val viewModel = MainActivityViewModel(bleScanner, bleManager)
        viewModel.scan()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assert(viewState.results.isNotEmpty())
    }

    @Test
    fun `Given an unknown ble device, when starting ble scan, then ble device is not shown`() =
        runTest {
            every { bleScanner.startScan() } returns flowOf()

            val viewModel = MainActivityViewModel(bleScanner, bleManager)
            viewModel.scan()

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assert(viewState.results.isEmpty())
        }

    @Test
    fun `Given an ble device, when connect to device, then ble device is connected`() = runTest {
        every { bleManager.connect(bluetoothDevice) } returns flowOf(ConnectionState.CONNECTED)

        val viewModel = MainActivityViewModel(bleScanner, bleManager)
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

            val viewModel = MainActivityViewModel(bleScanner, bleManager)
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

        val viewModel = MainActivityViewModel(bleScanner, bleManager)
        viewModel.scan()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assertEquals(
            ConnectionState.DISCONNECTED,
            viewState.results.first().connectionState
        )
    }
}
