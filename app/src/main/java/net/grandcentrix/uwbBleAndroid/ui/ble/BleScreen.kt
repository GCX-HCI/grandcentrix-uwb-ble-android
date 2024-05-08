package net.grandcentrix.uwbBleAndroid.ui.ble

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import net.grandcentrix.data.model.GcxBleConnectionState
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
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Connect to device") }) },
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
        ) {
            if (viewState.scanResults.isEmpty()) {
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
                    viewState.scanResults.forEach { device ->
                        DeviceItem(
                            device = device,
                            onDeviceClicked = onDeviceClicked,
                            onDisconnectClicked = onDisconnectClicked,
                            modifier = Modifier
                                .padding(vertical = 4.dp, horizontal = 16.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
            Button(
                onClick = onToggleScanClicked,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(text = if (viewState.isScanning) "Stop scan" else "Start scan")
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: GcxBleDevice,
    onDeviceClicked: (GcxBleDevice) -> Unit,
    onDisconnectClicked: () -> Unit,
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
            if (device.connectionState != GcxBleConnectionState.DISCONNECTED) {
                IconButton(onClick = onDisconnectClicked) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
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
        )
    }
}
