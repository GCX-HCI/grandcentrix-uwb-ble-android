package net.grandcentrix.uwbBleAndroid.ui.uwb

import androidx.core.uwb.RangingResult
import androidx.core.uwb.RangingResult.RangingResultPosition
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.grandcentrix.data.manager.UwbBleLibrary
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen

/**
 * @param distance The line-of-sight distance in meters of the ranging device, or null if not available.
 * @param azimuth The azimuth angle in degrees of the ranging device, or null if not available.
 * @param elevation The elevation angle in degrees of the ranging device, or null if not available.
 */
internal data class RangingUiState(
    val distance: Float? = null,
    val azimuth: Float? = null,
    val elevation: Float? = null
)

internal class RangingViewModel(
    private val uwbBleLibrary: UwbBleLibrary,
    private val navigator: Navigator
) : ViewModel() {
    private val _uiState = MutableStateFlow(RangingUiState())
    val uiState: StateFlow<RangingUiState> = _uiState.asStateFlow()

    // Only needed as the view model is not cleared due to very basic navigation implementation
    private var rangingJob: Job? = null

    fun onBackClicked() {
        onStop()
    }

    fun onResume() {
        collectUwbPositingResults()
    }

    fun onPause() {
        rangingJob?.cancel()
    }

    private fun collectUwbPositingResults() {
        rangingJob = viewModelScope.launch {
            uwbBleLibrary.startRanging().collect { rangingResult ->
                when (rangingResult) {
                    is RangingResultPosition -> updatePositionData(rangingResult)
                    is RangingResult.RangingResultPeerDisconnected -> onStop()
                }
            }
        }
    }

    private fun updatePositionData(positionResult: RangingResultPosition) {
        _uiState.update {
            it.copy(
                distance = positionResult.position.distance?.value ?: it.distance,
                azimuth = positionResult.position.azimuth?.value ?: it.azimuth,
                elevation = positionResult.position.elevation?.value ?: it.elevation
            )
        }
    }

    private fun onStop() {
        rangingJob?.cancel()
        navigator.navigateTo(Screen.Connect)
    }
}
