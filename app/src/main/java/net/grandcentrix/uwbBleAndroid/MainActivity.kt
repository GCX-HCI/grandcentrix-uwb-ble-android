package net.grandcentrix.uwbBleAndroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.Screen
import net.grandcentrix.uwbBleAndroid.ui.ble.BleScreen
import net.grandcentrix.uwbBleAndroid.ui.ranging.RangingScreen
import net.grandcentrix.uwbBleAndroid.ui.theme.AppTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val navigator: Navigator by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Very basic navigation implementation
                    val currentScreenState = navigator.currentScreen.collectAsState()

                    when (val currentScreen = currentScreenState.value) {
                        Screen.Connect -> BleScreen()
                        is Screen.Ranging -> {
                            RangingScreen(uwbDevice = currentScreen.uwbDevice)
                        }
                    }
                }
            }
        }
    }
}
