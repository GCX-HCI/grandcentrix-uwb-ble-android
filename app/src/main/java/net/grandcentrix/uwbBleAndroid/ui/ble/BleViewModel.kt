package net.grandcentrix.uwbBleAndroid.ui.ble

import android.util.Log
import androidx.annotation.RequiresPermission
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
import net.grandcentrix.api.data.manager.UwbBleLibrary
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.model.toGcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.AppPermissions
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen

data class BleViewState(
    val requestScanPermissions: Boolean = false,
    val requestConnectPermissions: Boolean = false,
    val isScanning: Boolean = false,
    val scanResults: Set<GcxBleDevice> = emptySet(),
    val connectingDevice: GcxBleDevice? = null
)

private const val MOBILE_KNOWLEDGE_ADDRESS = "00:60:37:90:E7:11"

class BleViewModel(
    private val uwbBleLibrary: UwbBleLibrary,
    private val permissionChecker: PermissionChecker,
    private val navigator: Navigator
) : ViewModel() {
    companion object {
        private val TAG = BleViewModel::class.simpleName
    }

    private val _viewState: MutableStateFlow<BleViewState> = MutableStateFlow(BleViewState())
    val viewState: StateFlow<BleViewState> = _viewState.asStateFlow()

    private var isScanPending: Boolean = false

    private var scanJob: Job? = null
    private var connectJob: Job? = null

    fun onScanPermissionsRequested() {
        _viewState.update { it.copy(requestScanPermissions = false) }
    }

    fun onConnectPermissionsRequested() {
        _viewState.update { it.copy(requestConnectPermissions = false) }
    }

    fun onToggleScanClicked() {
        if (_viewState.value.isScanning) {
            stopScan()
        } else {
            startScan()
        }
    }

    private fun startScan() {
        if (checkScanPermission()) {
            _viewState.update { it.copy(isScanning = true) }
            isScanPending = false
            scanJob = viewModelScope.launch {
                uwbBleLibrary.startScan()
                    .catch { error -> Log.e(TAG, "Failed to scan for devices ", error) }
                    .collect { scanResult ->
                        _viewState.update {
                            val updatedScanResults = buildSet {
                                addAll(it.scanResults)
                                add(scanResult.toGcxBleDevice())
                            }
                            it.copy(scanResults = updatedScanResults)
                        }
                    }
            }
        } else {
            isScanPending = true
            _viewState.update { it.copy(requestScanPermissions = true) }
        }
    }

    private fun stopScan() {
        _viewState.update { it.copy(isScanning = false) }
        scanJob?.cancel("User stopped ble scan")
    }

    fun onDeviceClicked(device: GcxBleDevice) {
        stopScan()
        _viewState.update { it.copy(connectingDevice = device) }
        if (checkConnectPermission()) {
            connectToDevice(device)
        } else {
            _viewState.update { it.copy(requestConnectPermissions = true) }
        }
    }

    fun onDisconnectClicked() {
        connectJob?.cancel()
        connectJob = null

        _viewState.update {
            it.copy(
                // Reset scan results to force re-scan
                scanResults = emptySet(),
                connectingDevice = null
            )
        }
    }

    fun onStartRangingClicked() {
        navigateToRangingScreen()
    }

    fun onPermissionResult() {
        if (isScanPending) return startScan()

        viewState.value.connectingDevice?.let { onDeviceClicked(it) }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private fun connectToDevice(device: GcxBleDevice) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            uwbBleLibrary.connect(device.bluetoothDevice)
                .catch {
                    Log.e(TAG, "Connection to $device failed", it)
                    // TODO: React on failed connection.
                }
                .collect { connectionState ->
                    _viewState.update {
                        it.copy(
                            connectingDevice = GcxBleDevice(device.bluetoothDevice, connectionState)
                        )
                    }
                }
        }
    }

    private fun checkScanPermission(): Boolean {
        return permissionChecker.hasPermissions(AppPermissions.bleScanPermissions)
    }

    private fun checkConnectPermission(): Boolean {
        return permissionChecker.hasPermissions(AppPermissions.bleConnectPermissions)
    }

    private fun navigateToRangingScreen() {
        navigator.navigateTo(Screen.Ranging)
    }
}
