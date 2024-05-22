package net.grandcentrix.lib.uwb.controlee

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
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.lib.ble.gatt.BleMessagingClient
import net.grandcentrix.lib.ble.gatt.GcxGattClient
import net.grandcentrix.lib.ble.model.BluetoothMessage
import net.grandcentrix.lib.protocol.OOBMessageProtocol
import net.grandcentrix.lib.uwb.exception.UwbException
import net.grandcentrix.lib.uwb.model.DeviceConfig
import net.grandcentrix.lib.uwb.model.RangingConfig
import net.grandcentrix.lib.uwb.model.UwbResult
import org.junit.jupiter.api.Assertions.assertInstanceOf
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
                uuid = UUID.fromString(GcxGattClient.UART_TX_CHARACTERISTIC),
                data = byteArrayOf(OOBMessageProtocol.UWB_DID_START.command),
                status = 0
            ),
            BluetoothMessage(
                uuid = UUID.fromString(GcxGattClient.UART_TX_CHARACTERISTIC),
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

    @Test
    fun `Given connected uwb device, when start ranging, then ranging started is received`() =
        runTest {
            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient
            )

            val result = controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            ).take(3)
                .toList()

            assertInstanceOf(UwbResult.RangingStarted::class.java, result[0])
            assertInstanceOf(UwbResult.PositionResult::class.java, result[1])
            assertInstanceOf(UwbResult.RangingStopped::class.java, result[2])

            coVerifyOrder {
                bleMessagingClient.enableReceiver()
                bleMessagingClient.messages
                deviceConfigInterceptor.intercept(any())
                bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
                uwbManager.controleeSessionScope()
                controleeSessionScope.localAddress
                phoneConfigInterceptor.intercept(any(), any(), any())
                bleMessagingClient.send(any())
            }
        }

    @Test
    fun `Given connected uwb device, when start ranging, then ranging positions are collected`() =
        runTest {
            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient
            )

            val result = controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            ).take(3)
                .toList()

            assertInstanceOf(UwbResult.RangingStarted::class.java, result[0])
            assertInstanceOf(UwbResult.PositionResult::class.java, result[1])
            assertInstanceOf(UwbResult.RangingStopped::class.java, result[2])

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
                bleMessagingClient
            )

            var error: Throwable? = null
            controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )
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
                bleMessagingClient
            )

            var error: Throwable? = null
            controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )
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
    fun `Given connected uwb device, when start command is not send, then throws null pointer exception`() =
        runTest {
            every { bleMessagingClient.messages } returns flowOf(
                BluetoothMessage(
                    uuid = UUID.fromString(GcxGattClient.UART_TX_CHARACTERISTIC),
                    data = byteArrayOf(OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command),
                    status = 0
                )
            )

            val controlee = GcxUwbControlee(
                uwbManager,
                bleMessagingClient
            )

            var error: Throwable? = null
            controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )
                .catch { error = it }
                .collect {}

            advanceUntilIdle()
            assertInstanceOf(UwbException.StartCommandNullException::class.java, error)

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
                bleMessagingClient
            )

            var error: Throwable? = null
            controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )
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
                bleMessagingClient
            )

            var error: Throwable? = null
            controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            )
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
                bleMessagingClient
            )

            val result = controlee.startRanging(
                deviceConfigInterceptor,
                phoneConfigInterceptor,
                rangingConfig
            ).take(3)
                .toList()

            assertInstanceOf(UwbResult.RangingStarted::class.java, result[0])
            assertInstanceOf(UwbResult.PositionResult::class.java, result[1])
            assertInstanceOf(UwbResult.RangingStopped::class.java, result[2])

            coVerify {
                bleMessagingClient.send(
                    byteArrayOf(OOBMessageProtocol.STOP_UWB_RANGING.command)
                )
            }
        }
}
