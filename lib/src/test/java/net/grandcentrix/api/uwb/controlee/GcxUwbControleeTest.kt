package net.grandcentrix.api.uwb.controlee

import android.util.Log
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbAddress
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.api.ble.manager.BleMessagingClient
import net.grandcentrix.api.ble.manager.GcxBleManager
import net.grandcentrix.api.ble.model.BluetoothMessage
import net.grandcentrix.api.ble.protocol.OOBMessageProtocol
import net.grandcentrix.api.uwb.exception.UwbException
import net.grandcentrix.api.uwb.model.DeviceConfig
import net.grandcentrix.api.uwb.model.RangingConfig
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GcxUwbControleeTest {
    private val uwbDevice: UwbDevice = mockk()
    private val controleeSessionScope: UwbControleeSessionScope = mockk {
        every { localAddress } returns UwbAddress("012345")
        every { prepareSession(any()) } returns flowOf(
            RangingResult.RangingResultPosition(
                uwbDevice,
                position = RangingPosition(null, null, null, 0L)
            )
        )
    }

    private val uwbManager: UwbManager = mockk {
        coEvery { controleeSessionScope() } returns controleeSessionScope
    }
    private val bleMessagingClient: BleMessagingClient = mockk {
        every { enableReceiver() } returns Result.success(true)
        every { messages } returns flowOf(
            BluetoothMessage(
                uuid = UUID.fromString(GcxBleManager.UART_TX_CHARACTERISTIC),
                data = byteArrayOf(OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command),
                status = 0
            )
        )
        coEvery { send(any()) } returns Result.success(Unit)
    }
    private val deviceConfigInterceptor: DeviceConfigInterceptor = mockk {
        every { intercept(any()) } returns DeviceConfig(deviceMacAddress = byteArrayOf())
    }
    private val phoneConfigInterceptor: PhoneConfigInterceptor = mockk {
        every { intercept(any(), any(), any()) } returns byteArrayOf()
    }

    private val rangingConfig: RangingConfig = RangingConfig(
        uwbConfigType = 0,
        sessionId = 0,
        subSessionId = 0,
        sessionKey = null,
        subSessionKey = null,
        channel = 0,
        preambleIndex = 0,
        updateRateType = 0
    )

    @BeforeEach
    fun setup() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
    }

    @Test
    fun `Given connected uwb device, when start ranging, then position result is received`() =
        runTest {
            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            val result = controlee.startRanging().first()
            assertInstanceOf(RangingResult.RangingResultPosition::class.java, result)

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
                deviceConfigInterceptor.intercept(any())
                bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
                uwbManager.controleeSessionScope()
                controleeSessionScope.localAddress
                phoneConfigInterceptor.intercept(any(), any(), any())
                bleMessagingClient.send(any())
                controleeSessionScope.prepareSession(any())
            }
        }

    @Test
    fun `Given connected non-uwb device, when start ranging, then DeciveConfigNullException is received`() =
        runTest {
            every { bleMessagingClient.messages } returns flowOf()

            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            var error: Throwable? = null
            controlee.startRanging()
                .catch { error = it }
                .collect {}

            advanceUntilIdle()
            assertInstanceOf(UwbException.DeciveConfigNullException::class.java, error)

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
            }
        }

    @Test
    fun `Given connected uwb device, when device config interception fails, then thrown error is received`() =
        runTest {
            every { deviceConfigInterceptor.intercept(any()) } throws Exception("Expected")

            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            var error: Throwable? = null
            controlee.startRanging()
                .catch { error = it }
                .collect {}

            advanceUntilIdle()
            assertInstanceOf(Exception::class.java, error)

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
                deviceConfigInterceptor.intercept(any())
            }
        }

    @Test
    fun `Given connected uwb device, when sending initialization message fails, then thrown is received`() =
        runTest {
            coEvery {
                bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
            } throws IllegalArgumentException("Expected")

            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            var error: Throwable? = null
            controlee.startRanging()
                .catch { error = it }
                .collect {}

            advanceUntilIdle()
            assertInstanceOf(IllegalArgumentException::class.java, error)

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
                deviceConfigInterceptor.intercept(any())
                bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
            }
        }

    @Test
    fun `Given connected uwb device, when phone config interception fails, then thrown is received`() =
        runTest {
            every {
                phoneConfigInterceptor.intercept(any(), any(), any())
            } throws RuntimeException("Expected")

            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            var error: Throwable? = null
            controlee.startRanging()
                .catch { error = it }
                .collect {}

            advanceUntilIdle()
            assertInstanceOf(RuntimeException::class.java, error)

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
                deviceConfigInterceptor.intercept(any())
                bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
                uwbManager.controleeSessionScope()
                controleeSessionScope.localAddress
                phoneConfigInterceptor.intercept(any(), any(), any())
            }
        }

    @Test
    fun `Given start session to uwb device, when stop ranging, then stop ranging command is send`() =
        runTest {
            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient,
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )

            val result = controlee.startRanging().first()
            assertInstanceOf(RangingResult.RangingResultPosition::class.java, result)

            coVerify {
                bleMessagingClient.send(
                    byteArrayOf(OOBMessageProtocol.STOP_UWB_RANGING.command)
                )
            }
        }
}
