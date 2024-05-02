package net.grandcentrix.uwbBleAndroid.ui.ranging

import androidx.core.uwb.RangingMeasurement
import androidx.core.uwb.RangingPosition
import androidx.core.uwb.RangingResult
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
import net.grandcentrix.data.manager.UwbBleLibrary
import net.grandcentrix.test.CoroutineTestExtension
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(CoroutineTestExtension::class)
class RangingViewModelTest {

    private val uwbBleLibrary: UwbBleLibrary = mockk {
        every { startRanging() } returns emptyFlow()
    }
    private val navigator: Navigator = mockk(relaxUnitFun = true)

    @Test
    fun `Given connected ranging device, when opening ranging screen, then collection of ranging events starts`() =
        runTest {
            val viewModel = RangingViewModel(uwbBleLibrary, navigator)
            viewModel.onResume()
            advanceUntilIdle()

            verify { uwbBleLibrary.startRanging() }
        }

    @Test
    fun `Given connected ranging device and ranging events are collected, when closing view, then navigate to Connect screen`() =
        runTest {
            val viewModel = RangingViewModel(uwbBleLibrary, navigator)
            viewModel.onResume()
            advanceUntilIdle()
            viewModel.onBackClicked()
            advanceUntilIdle()

            verify(ordering = Ordering.ORDERED) {
                uwbBleLibrary.startRanging()
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

            every { uwbBleLibrary.startRanging() } returns flowOf(
                RangingResult.RangingResultPosition(
                    mockk(),
                    position
                )
            )

            val viewModel = RangingViewModel(uwbBleLibrary, navigator)
            viewModel.onResume()
            advanceUntilIdle()

            assertEquals(
                RangingUiState(distance = 1f, azimuth = 2f, elevation = 3f),
                viewModel.uiState.value
            )

            verify { uwbBleLibrary.startRanging() }
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
                RangingResult.RangingResultPosition(uwbDevice, position)
            )
            every { uwbBleLibrary.startRanging() } returns rangingFlow

            val viewModel = RangingViewModel(uwbBleLibrary, navigator)
            viewModel.onResume()
            advanceUntilIdle()

            assertEquals(
                RangingUiState(distance = 1f, azimuth = 2f, elevation = 3f),
                viewModel.uiState.value
            )

            rangingFlow.update {
                RangingResult.RangingResultPosition(
                    uwbDevice,
                    RangingPosition(
                        distance = null,
                        azimuth = null,
                        elevation = null,
                        elapsedRealtimeNanos = 1L
                    )
                )
            }

            advanceUntilIdle()

            assertEquals(
                RangingUiState(distance = 1f, azimuth = 2f, elevation = 3f),
                viewModel.uiState.value
            )

            verify { uwbBleLibrary.startRanging() }
        }
}