package net.grandcentrix.uwbBleAndroid

import android.Manifest
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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.grandcentrix.data.manager.UwbBleManager
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.model.toGcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker

data class MainActivityViewState(
    val results: List<GcxBleDevice> = emptyList(),
    val requiredPermissions: Map<String, Boolean> = mapOf()
)

private const val MOBILE_KNOWLEDGE_ADDRESS = "00:60:37:90:E7:11"
private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(
    private val uwbBleManager: UwbBleManager,
    private val permissionChecker: PermissionChecker
) : ViewModel() {
    private val _viewState = MutableStateFlow(MainActivityViewState())
    val viewState: StateFlow<MainActivityViewState> = _viewState.asStateFlow()

    private var scanJob: Job? = null

    fun scan() {
        scanJob = viewModelScope.launch {
            if (checkScanPermission()) {
                uwbBleManager.startScan()
                    .filter { it.device.address == MOBILE_KNOWLEDGE_ADDRESS }
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
            if (checkBleConnectPermission()) {
                uwbBleManager.connect(bleDevice)
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

    fun updatePermissions(permission: String, isGranted: Boolean) {
        _viewState.update {
            val updatedPermissions = it.requiredPermissions.toMutableMap()
            updatedPermissions[permission] = isGranted
            it.copy(
                requiredPermissions = updatedPermissions.toMap()
            )
        }
    }

    private fun checkScanPermission(): Boolean = permissionChecker.hasPermissions(
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN)
    )

    private fun checkBleConnectPermission(): Boolean =
        permissionChecker.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)

    private fun MainActivityViewState.updateDeviceConnectionState(
        bleDevice: BluetoothDevice,
        connectionState: GcxBleConnectionState
    ): MainActivityViewState {
        val devices = this.results.toMutableList()
        val index = devices.indexOfFirst { it.bluetoothDevice.address == bleDevice.address }
        devices[index] = devices[index].copy(connectionState = connectionState)
        return this.copy(results = devices)
    }
    init {
        _viewState.update {
            it.copy(
                requiredPermissions = mapOf(
                    Manifest.permission.BLUETOOTH_SCAN to permissionChecker.hasPermission(
                        Manifest.permission.BLUETOOTH_SCAN
                    ),
                    Manifest.permission.ACCESS_FINE_LOCATION to permissionChecker.hasPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    Manifest.permission.BLUETOOTH_CONNECT to permissionChecker.hasPermission(
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            )
        }
    }
}
