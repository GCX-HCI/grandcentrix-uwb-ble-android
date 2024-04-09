package gcx.grandcentrix_uwb_ble_android

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gcx.ble.manager.BleManager
import gcx.ble.manager.ConnectionState
import gcx.ble.scanner.BleScanner
import gcx.grandcentrix_uwb_ble_android.model.GcxBleDevice
import gcx.grandcentrix_uwb_ble_android.model.toGcxBleDevice
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainActivityViewState(
    val results: List<GcxBleDevice> = mutableListOf(),
)

private const val mobileKnowledgeAddress = "00:60:37:90:E7:11"
private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(
    private val bleScanner: BleScanner,
    private val bleManager: BleManager,
) : ViewModel() {
    private val _viewState = MutableStateFlow(MainActivityViewState())
    val viewState: StateFlow<MainActivityViewState> = _viewState.asStateFlow()

    private var scanJob: Job? = null

    fun scan() {
        scanJob = viewModelScope.launch {
            bleScanner.startScan()
                .catch { error ->
                    Log.e(TAG, "Failed to scan for devices ", error)
                }
                .collect { result ->
                    _viewState.update {
                        val newResults = listOf(result.toGcxBleDevice())
                        it.copy(
                            results = it.results + newResults,
                        )
                    }
                }
        }
    }

    fun stopScan() {
        scanJob?.cancel("stop scanning")
    }

    fun connectToDevice(bleDevice: BluetoothDevice) {
        viewModelScope.launch {
            bleManager.connect(bleDevice)
                .collect { connectionState ->
                    _viewState.update {
                        updateDeviceConnectionState(
                            viewState = it,
                            bleDevice = bleDevice,
                            connectionState = connectionState
                        )
                    }
                }
        }
    }

    private fun MainActivityViewState.updateDeviceConnectionState(
        bleDevice: BluetoothDevice,
        connectionState: ConnectionState
    ): MainActivityViewState {
        val devices = viewState.results.toMutableList()
        val index = devices.indexOfFirst { it.bluetoothDevice.address == bleDevice.address } 
        devices[index] = devices[index].copy(
                connectionState = connectionState
            )
        return viewState.copy(
            results = devices
        )
    }
}
