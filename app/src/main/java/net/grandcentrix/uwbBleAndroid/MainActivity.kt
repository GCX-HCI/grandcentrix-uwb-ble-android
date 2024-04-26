package net.grandcentrix.uwbBleAndroid

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import net.grandcentrix.uwbBleAndroid.model.GcxBleDevice
import net.grandcentrix.uwbBleAndroid.ui.theme.GrandcentrixuwbbleandroidTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrandcentrixuwbbleandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScanScreen()
                }
            }
        }
    }
}

@Composable
fun ScanScreen(viewModel: MainActivityViewModel = koinViewModel()) {
    val viewState by viewModel.viewState.collectAsState()
    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { entry ->
            entry.forEach { (permission, isGranted) ->
                viewModel.updatePermissions(permission = permission, isGranted = isGranted)
            }
        }
    )
    Column {
        Row {
            Button(onClick = {
                if (viewState.requiredPermissions.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        false
                    ) && viewState.requiredPermissions.getOrDefault(
                        Manifest.permission.BLUETOOTH_SCAN,
                        false
                    )
                ) {
                    viewModel.scan()
                } else {
                    scanPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH_SCAN
                        )
                    )
                }
            }) {
                Text(text = "Start scan")
            }
            Button(onClick = { viewModel.stopScan() }) {
                Text(text = "Stop scan")
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            viewState.results.forEach { device ->
                DeviceItem(
                    device = device,
                    isConnectPermissionGranted = viewState.requiredPermissions.getOrDefault(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        false
                    ),
                    updatePermission = viewModel::updatePermissions,
                    onItemClicked = viewModel::connectToDevice
                )
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: GcxBleDevice,
    isConnectPermissionGranted: Boolean,
    updatePermission: (String, Boolean) -> Unit,
    onItemClicked: (BluetoothDevice) -> Unit
) {
    val connectPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            updatePermission(
                Manifest.permission.BLUETOOTH_CONNECT,
                isGranted
            )
        }
    )
    OutlinedButton(onClick = {
        if (isConnectPermissionGranted) {
            onItemClicked(device.bluetoothDevice)
        } else {
            connectPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }) {
        Column {
            Text(text = "Address: ${device.bluetoothDevice.address}")
            Text(text = "Connection state: ${device.connectionState}")
        }
    }
}
