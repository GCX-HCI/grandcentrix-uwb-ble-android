package net.grandcentrix.uwbBleAndroid.ui.ranging

import android.Manifest
import androidx.core.uwb.RangingMeasurement
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.UwbDevice
import io.mockk.Ordering
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.grandcentrix.lib.ble.model.GcxUwbDevice
import net.grandcentrix.lib.uwb.model.UwbResult
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import net.grandcentrix.uwbBleAndroid.testx.CoroutineTestExtension
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class RangingViewModelTest {

    private val gcxUwbDevice: GcxUwbDevice = mockk {
        every { startRanging(any(), any(), any()) } returns emptyFlow()
    }

    private val navigator: Navigator = mockk(relaxUnitFun = true)

    private val permissionChecker: PermissionChecker = mockk {
        every { hasPermissions(any()) } returns true
    }

    @Test
    fun `Given connected ranging device, when opening ranging screen, then collection of ranging events starts`() =
        runTest {
            val viewModel = RangingViewModel(gcxUwbDevice, navigator, permissionChecker)
            viewModel.onResume()
            advanceUntilIdle()

            verify {
                gcxUwbDevice.startRanging(any(), any(), any())
            }
        }

    @Test
    fun `Given connected ranging device and ranging events are collected, when closing view, then navigate to Connect screen`() =
        runTest {
            val viewModel = RangingViewModel(gcxUwbDevice, navigator, permissionChecker)
            viewModel.onResume()
            advanceUntilIdle()
            viewModel.onBackClicked()
            advanceUntilIdle()

            verify(ordering = Ordering.ORDERED) {
                gcxUwbDevice.startRanging(any(), any(), any())
                navigator.navigateTo(Screen.Connect)
            }
        }

    @Test
    fun `Given ranging events are collected, when a position event is received, then show position`() =
        runTest {
            val position = RangingPosition(
                distance = RangingMeasurement(1f),
                azimuth = RangingMeasurement(2f),
                elevation = RangingMeasurement(3f),
                elapsedRealtimeNanos = 0L
            )

            every {
                gcxUwbDevice.startRanging(any(), any(), any())
            } returns flowOf(
                UwbResult.PositionResult(
                    distance = position.distance,
                    azimuth = position.azimuth,
                    elevation = position.elevation,
                    elapsedRealtimeNanos = position.elapsedRealtimeNanos
                )
            )

            val viewModel = RangingViewModel(gcxUwbDevice, navigator, permissionChecker)
            viewModel.onResume()
            advanceUntilIdle()

            assertEquals(
                RangingUiState(
                    distance = 1f,
                    azimuth = 2f,
                    elevation = 3f,
                    isRangingPeerConnected = true
                ),
                viewModel.uiState.value
            )

            verify {
                gcxUwbDevice.startRanging(any(), any(), any())
            }
        }

    @Test
    fun `Given ranging events are collected, when a position event contains null values, then ignore null values`() =
        runTest {
            val position = RangingPosition(
                distance = RangingMeasurement(1f),
                azimuth = RangingMeasurement(2f),
                elevation = RangingMeasurement(3f),
                elapsedRealtimeNanos = 0L
            )

            val uwbDevice: UwbDevice = mockk()
            val rangingFlow = MutableStateFlow(
                UwbResult.PositionResult(
                    distance = position.distance,
                    azimuth = position.azimuth,
                    elevation = position.elevation,
                    elapsedRealtimeNanos = position.elapsedRealtimeNanos
                )
            )
            every {
                gcxUwbDevice.startRanging(any(), any(), any())
            } returns rangingFlow

            val viewModel = RangingViewModel(gcxUwbDevice, navigator, permissionChecker)
            viewModel.onResume()
            advanceUntilIdle()

            assertEquals(
                RangingUiState(
                    distance = 1f,
                    azimuth = 2f,
                    elevation = 3f,
                    isRangingPeerConnected = true
                ),
                viewModel.uiState.value
            )

            rangingFlow.update {
                UwbResult.PositionResult(
                    distance = null,
                    azimuth = null,
                    elevation = null,
                    elapsedRealtimeNanos = 0L
                )
            }

            advanceUntilIdle()

            assertEquals(
                RangingUiState(
                    distance = 1f,
                    azimuth = 2f,
                    elevation = 3f,
                    isRangingPeerConnected = true
                ),
                viewModel.uiState.value
            )

            verify {
                gcxUwbDevice.startRanging(any(), any(), any())
            }
        }

    @Test
    fun `Given uwb permission not granted, when opening ranging screen, then uwbControlee startRanging is not called`() =
        runTest {
            every {
                permissionChecker.hasPermissions(
                    listOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.UWB_RANGING
                    )
                )
            } returns false
            val viewModel = RangingViewModel(gcxUwbDevice, navigator, permissionChecker)
            viewModel.onResume()
            advanceUntilIdle()

            verify(exactly = 0) {
                gcxUwbDevice.startRanging(any(), any(), any())
            }
        }
}
