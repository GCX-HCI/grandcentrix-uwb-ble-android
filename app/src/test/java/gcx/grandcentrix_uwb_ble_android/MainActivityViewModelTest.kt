package gcx.grandcentrix_uwb_ble_android

import android.bluetooth.le.ScanResult
import gcx.ble.scanner.BleScanner
import gcx.test.CoroutineTestExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class MainActivityViewModelTest {

    private val scanResult: ScanResult = mockk {
        every { device.address } returns "00:60:37:90:E7:11" // hardcoded mac address for mobile knowledge dev kit
    }
    private val bleScanner: BleScanner = mockk {
        every { startScan() } returns flowOf(scanResult)
    }

    @Test
    fun `Given a known ble device, when starting ble scan, then ble device is shown`() = runTest {
        val viewModel = MainActivityViewModel(bleScanner)
        viewModel.scan()

        advanceUntilIdle()

        val viewState = viewModel.viewState.value
        assert(viewState.results.isNotEmpty())
    }

    @Test
    fun `Given an unknown ble device, when starting ble scan, then ble device is not shown`() =
        runTest {
            every { scanResult.device.address } returns "Unknown"

            val viewModel = MainActivityViewModel(bleScanner)
            viewModel.scan()

            advanceUntilIdle()

            val viewState = viewModel.viewState.value
            assert(viewState.results.isEmpty())
        }
}