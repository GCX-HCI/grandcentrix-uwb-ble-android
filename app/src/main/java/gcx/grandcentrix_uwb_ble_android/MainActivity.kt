package gcx.grandcentrix_uwb_ble_android

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import gcx.ble.manager.ConnectionState
import gcx.grandcentrix_uwb_ble_android.model.GcxBleDevice
import gcx.grandcentrix_uwb_ble_android.ui.theme.GrandcentrixuwbbleandroidTheme
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GrandcentrixuwbbleandroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ScanScreen()
                }
            }
        }
    }
}

@Composable
fun ScanScreen(viewModel: MainActivityViewModel = getViewModel()) {
    val viewState by viewModel.viewState.collectAsState()

    Column {
        Row {
            Button(onClick = { viewModel.scan() }) {
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
                    onItemClicked = viewModel::connectToDevice,
                )
            }
        }
    }
}

@Composable
fun DeviceItem(
    device: GcxBleDevice,
    onItemClicked: (BluetoothDevice) -> Unit,
) {
    OutlinedButton(onClick = { onItemClicked(device.bluetoothDevice) }) {
        Column {
            Text(text = "Address: ${device.bluetoothDevice.address}")
            Text(text = "Connection state: ${device.connectionState}")
        }
    }
}
