@file:OptIn(ExperimentalCoroutinesApi::class)

package gcx.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import gcx.ble.exception.BluetoothDisabledException
import gcx.ble.manager.BleManager
import gcx.ble.scanner.GcxBleScanner
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GcxBleScannerTest {
    private lateinit var bleManager: BleManager
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var scanResultMock: ScanResult

    @BeforeEach
    fun setup() {
        bluetoothLeScanner = mockk()
        bluetoothAdapter = mockk()
        scanResultMock = mockk()

        bleManager =
            mockk {
                every { bluetoothAdapter() } returns bluetoothAdapter
                every { bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner
            }
    }

    private fun createMock(isBleEnabled: Boolean) {
        every { bleManager.bluetoothAdapter().isEnabled } returns isBleEnabled
        justRun { bluetoothLeScanner.startScan(any()) }
        justRun { bluetoothLeScanner.stopScan(any<ScanCallback>()) }
    }

    @Test
    fun `Given ble is disabled, when start ble scan, then a error should be thrown`() =
        runTest {
            createMock(isBleEnabled = false)

            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            var errorWasThrown: BluetoothDisabledException? = null
            gcxBleScanner.startScan()
                .catch { errorWasThrown = it as BluetoothDisabledException }
                .collect()
            advanceUntilIdle()
            assertNotNull(errorWasThrown)
        }

    @Test
    fun `Given permission is denied, when start ble scan, then a error should be thrown`() =
        runTest {
            mockkStatic(Log::class)
            createMock(isBleEnabled = true)
            every { bleManager.bluetoothAdapter().isEnabled } returns true
            every { bluetoothLeScanner.startScan(any()) } throws SecurityException("Permission missing!")
            every { Log.d(any(), any()) } returns 0
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            var errorWasThrown = false
            gcxBleScanner.startScan()
                .catch { errorWasThrown = true }
                .collect()
            advanceUntilIdle()
            assertTrue(errorWasThrown)
        }

    @Test
    fun `Given ble is enabled, when start ble scan, then bluetoothLeScan should call startScan()`() =
        runTest {
            createMock(isBleEnabled = true)

            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            val job = launch { gcxBleScanner.startScan().collect() }
            job.start()
            delay(1_000)
            job.cancel()
            verify { bluetoothLeScanner.startScan(any()) }
        }

    private fun triggerScanCallback(callback: ScanCallback) {
        callback.onScanResult(
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES,
            scanResultMock,
        )
    }

    @Test
    fun `Given ble is enabled, when start ble scan, then return scan result`() =
        runTest {
            val scanCallback: ScanCallback = mockk()
            createMock(isBleEnabled = true)

            every { scanCallback.onScanResult(any(), any()) } answers {
                val result = arg<ScanResult>(1)
                println("scan result: $result")
            }
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            triggerScanCallback(scanCallback)

            val job = launch { gcxBleScanner.startScan().collect() }
            job.start()
            delay(1_000)
            job.cancel()
            verify {
                scanCallback.onScanResult(
                    ScanSettings.CALLBACK_TYPE_ALL_MATCHES,
                    scanResultMock,
                )
            }
        }
}
