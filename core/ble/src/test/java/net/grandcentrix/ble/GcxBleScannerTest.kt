@file:OptIn(ExperimentalCoroutinesApi::class)

package net.grandcentrix.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.ble.exception.BluetoothException
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.scanner.GcxBleScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class GcxBleScannerTest {
    private val bluetoothAdapter: BluetoothAdapter = mockk()
    private val bluetoothLeScanner: BluetoothLeScanner = mockk()
    private val bleManager: BleManager =
        mockk {
            every { bluetoothAdapter() } returns bluetoothAdapter
            every { bluetoothAdapter().bluetoothLeScanner } returns bluetoothLeScanner
            every { bluetoothAdapter().isEnabled } returns true
            justRun { bluetoothLeScanner.startScan(any()) }
            justRun { bluetoothLeScanner.stopScan(any<ScanCallback>()) }
        }

    private val scanResultMock: ScanResult = mockk()

    @Test
    fun `Given ble is disabled, when start ble scan, then a error should be thrown`() = runTest {
        every { bleManager.bluetoothAdapter().isEnabled } returns false

        val gcxBleScanner =
            GcxBleScanner(
                bleManager = bleManager
            )

        var thrownError: Throwable? = null
        gcxBleScanner.startScan()
            .catch { thrownError = it }
            .collect()
        advanceUntilIdle()
        assertEquals(BluetoothException.BluetoothDisabledException, thrownError)
    }

    @Test
    fun `Given permission is denied, when start ble scan, then a error should be thrown`() =
        runTest {
            mockkStatic(Log::class)

            every { bluetoothLeScanner.startScan(any()) } throws SecurityException()
            every { Log.e(any(), any()) } returns 0

            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager
                )

            var thrownError: Throwable? = null
            gcxBleScanner.startScan()
                .catch { thrownError = it }
                .collect()
            advanceUntilIdle()
            assertInstanceOf(SecurityException::class.java, thrownError)
        }

    @Test
    fun `Given ble is enabled, when start ble scan, then bluetoothLeScan should call startScan()`() =
        runTest {
            val gcxBleScanner =
                GcxBleScanner(
                    bleManager = bleManager
                )

            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                gcxBleScanner.startScan().collect()
            }
            advanceUntilIdle()
            verify { bluetoothLeScanner.startScan(any()) }
        }

    private fun triggerScanCallback(callback: ScanCallback) {
        callback.onScanResult(
            ScanSettings.CALLBACK_TYPE_ALL_MATCHES,
            scanResultMock
        )
    }

    @Test
    fun `Given ble is enabled, when start ble scan, then return scan result`() = runTest {
        val scanCallback: ScanCallback = mockk()

        every { scanCallback.onScanResult(any(), any()) } answers {
            val result = arg<ScanResult>(1)
            println("scan result: $result")
        }
        val gcxBleScanner =
            GcxBleScanner(
                bleManager = bleManager
            )

        triggerScanCallback(scanCallback)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            gcxBleScanner.startScan().collect()
        }
        advanceUntilIdle()
        verify {
            scanCallback.onScanResult(
                ScanSettings.CALLBACK_TYPE_ALL_MATCHES,
                scanResultMock
            )
        }
    }
}
