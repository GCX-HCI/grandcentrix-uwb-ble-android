package net.grandcentrix.uwbBleAndroid.ui.ble

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.grandcentrix.api.ble.model.ConnectionState
import net.grandcentrix.api.ble.model.GcxUwbDevice
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.permission.AppPermissions
import net.grandcentrix.uwbBleAndroid.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun BleScreen(viewModel: BleViewModel = koinViewModel()) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.onPermissionResult() }
    )

    val viewState by viewModel.viewState.collectAsState()
    BleView(
        viewState = viewState,
        onToggleScanClicked = viewModel::onToggleScanClicked,
        onDeviceClicked = viewModel::onDeviceClicked,
        onDisconnectClicked = viewModel::onDisconnectClicked,
        onStartRangingClicked = viewModel::onStartRangingClicked
    )

    if (viewState.requestScanPermissions) {
        viewModel.onScanPermissionsRequested()
        permissionLauncher.launch(AppPermissions.bleScanPermissions.toTypedArray())
    }

    if (viewState.requestConnectPermissions) {
        viewModel.onConnectPermissionsRequested()
        permissionLauncher.launch(AppPermissions.bleConnectPermissions.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleView(
    viewState: BleViewState,
    onToggleScanClicked: () -> Unit,
    onDeviceClicked: (GcxBleDevice) -> Unit,
    onDisconnectClicked: () -> Unit,
    onStartRangingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Connect to device") }) },
        modifier = modifier
    ) { innerPadding ->
        Crossfade(
            targetState = viewState.connectingDevice,
            label = "Cross fade between scan and connect",
            modifier = Modifier.padding(innerPadding)
        ) { connectingDevice ->
            if (connectingDevice == null) {
                ScanResultsView(
                    viewState.scanResults,
                    viewState.isScanning,
                    onDeviceClicked,
                    onToggleScanClicked
                )
            } else {
                ConnectionView(
                    connectingDevice,
                    onDisconnectClicked,
                    onStartRangingClicked
                )
            }
        }
    }
}

@Composable
private fun ScanResultsView(
    scanResults: Set<GcxBleDevice>,
    isScanning: Boolean,
    onDeviceClicked: (GcxBleDevice) -> Unit,
    onToggleScanClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        if (scanResults.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(text = "No devices found yet.")
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                scanResults.forEach { device ->
                    ScanResultItem(
                        device = device,
                        onDeviceClicked = onDeviceClicked,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = onToggleScanClicked) {
                Text(text = if (isScanning) "Stop scan" else "Start scan")
            }
        }
    }
}

@Composable
fun ScanResultItem(
    device: GcxBleDevice,
    onDeviceClicked: (GcxBleDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onDeviceClicked(device) },
        modifier = modifier
    ) {
        Row {
            Column {
                Text(text = "Address: ${device.bluetoothDevice.address}")
                Text(text = "Connection state: ${device.connectionState}")
            }
        }
    }
}

@Composable
private fun ConnectionView(
    device: GcxBleDevice,
    onDisconnectClicked: () -> Unit,
    onStartRangingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        ScanResultItem(device = device, onDeviceClicked = {})

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            OutlinedButton(onClick = onDisconnectClicked) {
                Text(text = "Disconnect")
            }

            Crossfade(
                targetState = device.connectionState,
                label = "Cross fade between loading and established connection"
            ) { connectionState ->
                if (connectionState is ConnectionState.ServicesDiscovered) {
                    Button(onClick = onStartRangingClicked) {
                        Text(text = "Start ranging")
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview
@Composable
private fun BleScreenPreview() {
    AppTheme {
        BleView(
            viewState = BleViewState(),
            onToggleScanClicked = {},
            onDeviceClicked = {},
            onDisconnectClicked = {},
            onStartRangingClicked = {}
        )
    }
}
