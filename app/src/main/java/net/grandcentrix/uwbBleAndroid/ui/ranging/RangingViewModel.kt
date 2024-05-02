package net.grandcentrix.uwbBleAndroid.ui.ranging

import android.util.Log
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
    val elevation: Float? = null,
    val isRangingPeerConnected: Boolean = false
)

internal class RangingViewModel(
    private val uwbBleLibrary: UwbBleLibrary,
    private val navigator: Navigator
) : ViewModel() {
    companion object {
        private val TAG = RangingViewModel::class.java.simpleName
    }

    private val _uiState = MutableStateFlow(RangingUiState())
    val uiState: StateFlow<RangingUiState> = _uiState.asStateFlow()

    // Only needed as the view model is not cleared due to very basic navigation implementation
    private var rangingJob: Job? = null

    fun onBackClicked() {
        stopRanging()
        navigator.navigateTo(Screen.Connect)
    }

    fun onResume() {
        collectUwbPositingResults()
    }

    fun onPause() {
        stopRanging()
    }

    private fun collectUwbPositingResults() {
        rangingJob = viewModelScope.launch {
            uwbBleLibrary.startRanging().collect { rangingResult ->
                when (rangingResult) {
                    is RangingResultPosition -> updatePositionData(rangingResult)
                    is RangingResult.RangingResultPeerDisconnected -> onDisconnected()
                }
            }
        }
    }

    private fun updatePositionData(positionResult: RangingResultPosition) {
        _uiState.update {
            it.copy(
                distance = positionResult.position.distance?.value ?: it.distance,
                azimuth = positionResult.position.azimuth?.value ?: it.azimuth,
                elevation = positionResult.position.elevation?.value ?: it.elevation,
                isRangingPeerConnected = true
            )
        }
    }

    private fun onDisconnected() {
        Log.d(TAG, "Ranging device disconnected!")
        _uiState.update { it.copy(isRangingPeerConnected = false) }
        stopRanging()
    }

    private fun stopRanging() {
        rangingJob?.cancel()
        _uiState.update { it.copy(isRangingPeerConnected = false) }
    }
}
