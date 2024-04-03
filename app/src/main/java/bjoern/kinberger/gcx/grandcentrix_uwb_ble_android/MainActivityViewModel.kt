package bjoern.kinberger.gcx.grandcentrix_uwb_ble_android

import android.bluetooth.le.ScanResult
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bjoern.kinberger.gcx.ble.scanner.BleScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainActivityViewState(
    val results: List<ScanResult> = mutableListOf(),
)

class MainActivityViewModel(
    private val bleScanner: BleScanner,
) : ViewModel() {
    private val _viewState = MutableStateFlow(MainActivityViewState())
    val viewState: StateFlow<MainActivityViewState> = _viewState.asStateFlow()

    fun scan() {
        viewModelScope.launch {
            bleScanner.startScan(
                onScanFailure = { error ->
                    Log.d("TAG", "scan: $error")
                },
            )
                .filter { it.device.address == "00:60:37:90:E7:11" }
                .collect { result ->
                    _viewState.update {
                        val newResults = listOf(result)
                        it.copy(
                            results = it.results + newResults,
                        )
                    }
                }
        }
    }
}
