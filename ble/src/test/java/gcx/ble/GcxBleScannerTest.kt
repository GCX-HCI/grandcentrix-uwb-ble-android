@file:OptIn(ExperimentalCoroutinesApi::class)

package gcx.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import gcx.ble.manager.BleManager
import gcx.ble.scanner.GcxBleScanner
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
        bleManager = mockk()
        bluetoothLeScanner = mockk()
        bluetoothAdapter = mockk()
        scanResultMock = mockk()
    }

    @Test
    fun `startScan with ble not enabled should throw error`() =
        runTest {
            every { bleManager.bluetoothAdapter() } returns bluetoothAdapter
            every { bleManager.bluetoothAdapter().isEnabled } returns false
            every { bleManager.bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner

            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            var errorWasThrown = false
            gcxBleScanner.startScan {
                errorWasThrown = true
            }.collect()
            advanceUntilIdle()
            assertTrue(errorWasThrown)
        }

    @Test
    fun `startScan with no permission should throw SecurityException`() =
        runTest {
            mockkStatic(Log::class)
            every { bleManager.bluetoothAdapter() } returns bluetoothAdapter
            every { bleManager.bluetoothAdapter().isEnabled } returns true
            every { bleManager.bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner
            every { bluetoothLeScanner.startScan(any()) } throws SecurityException("Permission missing!")
            justRun { bluetoothLeScanner.stopScan(any<ScanCallback>()) }
            every { Log.d(any(), any()) } returns 0
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            var errorWasThrown = false
            val job =
                launch {
                    gcxBleScanner.startScan {
                        errorWasThrown = true
                    }.collect()
                }
            job.start()
            delay(1_000)
            job.cancel()
            assertTrue(errorWasThrown)
        }

    @Test
    fun `startScan with ble enabled should call bluetoothLeScanner startScan()`() =
        runTest {
            every { bleManager.bluetoothAdapter() } returns bluetoothAdapter
            every { bleManager.bluetoothAdapter().isEnabled } returns true
            every { bleManager.bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner
            justRun { bluetoothLeScanner.startScan(any()) }
            justRun { bluetoothLeScanner.stopScan(any<ScanCallback>()) }
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            val job = launch { gcxBleScanner.startScan { }.collect() }
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
    fun `startScan with ble enabled should return scan result`() =
        runTest {
            val scanCallback: ScanCallback = mockk()
            every { bleManager.bluetoothAdapter() } returns bluetoothAdapter
            every { bleManager.bluetoothAdapter().isEnabled } returns true
            every { bleManager.bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner
            justRun { bluetoothLeScanner.startScan(any()) }
            justRun { bluetoothLeScanner.stopScan(any<ScanCallback>()) }

            every { scanCallback.onScanResult(any(), any()) } answers {
                val result = arg<ScanResult>(1)
                println("scan result: $result")
            }
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager,
                )

            triggerScanCallback(scanCallback)

            val job = launch { gcxBleScanner.startScan { }.collect() }
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
