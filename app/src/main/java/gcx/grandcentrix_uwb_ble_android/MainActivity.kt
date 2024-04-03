package gcx.grandcentrix_uwb_ble_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        }
        viewState.results.forEach {result -> 
            Text(text = "Address: ${result.device.address}")
        }
    }
}
