package net.grandcentrix.uwbBleAndroid.ui.ble

import android.util.Log
import androidx.core.uwb.RangingResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.grandcentrix.data.manager.UwbBleLibrary
import net.grandcentrix.data.model.GcxBleConnectionState
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.model.toGcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.AppPermissions
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker

data class BleViewState(
    val requestScanPermissions: Boolean = false,
    val requestConnectPermissions: Boolean = false,
    val isScanning: Boolean = false,
    val scanResults: Set<GcxBleDevice> = emptySet(),
    val isConnecting: Boolean = false
)

private const val MOBILE_KNOWLEDGE_ADDRESS = "00:60:37:90:E7:11"

class BleViewModel(
    private val uwbBleLibrary: UwbBleLibrary,
    private val permissionChecker: PermissionChecker
) : ViewModel() {
    companion object {
        private val TAG = BleViewModel::class.simpleName
    }

    private val _viewState: MutableStateFlow<BleViewState> = MutableStateFlow(BleViewState())
    val viewState: StateFlow<BleViewState> = _viewState.asStateFlow()

    private var isScanPending: Boolean = false
    private var deviceConnectPending: GcxBleDevice? = null

    private var scanJob: Job? = null

    fun onScanPermissionsRequested() {
        _viewState.update { it.copy(requestScanPermissions = false) }
    }

    fun onConnectPermissionsRequested() {
        _viewState.update { it.copy(requestScanPermissions = false) }
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
        if (checkConnectPermission() && !viewState.value.isConnecting) {
            _viewState.update { it.copy(isConnecting = true) }
            deviceConnectPending = null
            viewModelScope.launch {
                uwbBleLibrary.connect(device.bluetoothDevice)
                    .catch { Log.e(TAG, "Connection to $device failed", it) }
                    .collect { connectionState ->
                        updateConnectionState(device, connectionState)

                        if (connectionState == GcxBleConnectionState.SERVICES_DISCOVERED) {
                            launch {
                                uwbBleLibrary.startRanging().collect { rangingResult ->
                                    when (rangingResult) {
                                        is RangingResult.RangingResultPosition -> Log.d(
                                            TAG,
                                            "Position: ${rangingResult.position.distance}"
                                        )
                                        is RangingResult.RangingResultPeerDisconnected -> Log.e(
                                            TAG,
                                            "Peer disconnected ${rangingResult.device.address.address.contentToString()}"
                                        )
                                    }
                                }
                            }
                        }
                    }
            }
        } else {
            deviceConnectPending = device
            _viewState.update { it.copy(requestConnectPermissions = true) }
        }
    }

    fun onPermissionResult() {
        if (isScanPending) {
            return startScan()
        }

        deviceConnectPending?.let { device -> onDeviceClicked(device) }
    }

    private fun updateConnectionState(
        device: GcxBleDevice,
        connectionState: GcxBleConnectionState
    ) {
        _viewState.update {
            val updatedResults = buildSet {
                add(
                    GcxBleDevice(
                        bluetoothDevice = device.bluetoothDevice,
                        connectionState = connectionState
                    )
                )

                addAll(
                    it.scanResults
                        .filterNot { scannedDevice ->
                            scannedDevice.bluetoothDevice == device.bluetoothDevice
                        }
                )
            }
            it.copy(scanResults = updatedResults)
        }
    }

    private fun checkScanPermission(): Boolean {
        return permissionChecker.hasPermissions(AppPermissions.bleScanPermissions)
    }

    private fun checkConnectPermission(): Boolean {
        return permissionChecker.hasPermissions(AppPermissions.bleConnectPermissions)
    }
}
