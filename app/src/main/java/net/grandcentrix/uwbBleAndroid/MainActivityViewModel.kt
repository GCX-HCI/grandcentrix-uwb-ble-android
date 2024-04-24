package net.grandcentrix.uwbBleAndroid

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.manager.ConnectionState
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.model.toGcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker

data class MainActivityViewState(
    val results: List<GcxBleDevice> = emptyList()
)

private const val mobileKnowledgeAddress = "00:60:37:90:E7:11"
private const val TAG = "MainActivityViewModel"

@SuppressLint("MissingPermission")
class MainActivityViewModel(
    private val bleScanner: BleScanner,
    private val bleManager: BleManager,
    private val permissionChecker: PermissionChecker
) : ViewModel() {
    private val _viewState = MutableStateFlow(MainActivityViewState())
    val viewState: StateFlow<MainActivityViewState> = _viewState.asStateFlow()

    private var scanJob: Job? = null

    fun scan() {
        scanJob = viewModelScope.launch {
            if (permissionChecker.hasPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            ) {
                bleScanner.startScan()
                    .catch { error -> Log.e(TAG, "Failed to scan for devices ", error) }
                    .collect { result ->
                        _viewState.update {
                            val newResults = listOf(result.toGcxBleDevice())
                            it.copy(results = it.results + newResults)
                        }
                    }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel("stop scanning")
    }

    fun connectToDevice(bleDevice: BluetoothDevice) {
        viewModelScope.launch {
            if (permissionChecker.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                bleManager.connect(bleDevice)
                    .catch { Log.e(TAG, "connectToDevice failed", it) }
                    .collect { connectionState ->
                        _viewState.update {
                            it.updateDeviceConnectionState(
                                bleDevice = bleDevice,
                                connectionState = connectionState
                            )
                        }
                    }
            }
        }
    }

    private fun MainActivityViewState.updateDeviceConnectionState(
        bleDevice: BluetoothDevice,
        connectionState: ConnectionState
    ): MainActivityViewState {
        val devices = this.results.toMutableList()
        val index = devices.indexOfFirst { it.bluetoothDevice.address == bleDevice.address }
        devices[index] = devices[index].copy(connectionState = connectionState)
        return this.copy(results = devices)
    }

    fun isScanPermissionGranted(): Boolean = permissionChecker.hasPermissions(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN)
    )

    fun isConnectPermissionGranted(): Boolean =
        permissionChecker.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
}
