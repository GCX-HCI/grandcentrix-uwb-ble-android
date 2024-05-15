package net.grandcentrix.uwbBleAndroid.ui.ranging

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import net.grandcentrix.lib.ble.model.GcxUwbDevice
import net.grandcentrix.uwbBleAndroid.permission.AppPermissions
import net.grandcentrix.uwbBleAndroid.ui.theme.AppTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun RangingScreen(
    uwbDevice: GcxUwbDevice,
    viewModel: RangingViewModel = koinViewModel(parameters = { parametersOf(uwbDevice) })
) {
    BackHandler { viewModel.onBackClicked() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.onPermissionResult() }
    )

    LifecycleResumeEffect {
        viewModel.onResume()

        onPauseOrDispose {
            viewModel.onPause()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    RangingView(
        uiState = uiState,
        onBackClicked = viewModel::onBackClicked
    )

    if (uiState.requestUwbRangingPermission) {
        viewModel.onUwbRangingPermissionRequested()
        permissionLauncher.launch(AppPermissions.uwbRangingPermissions.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangingView(uiState: RangingUiState, onBackClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Ranging") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp)
        ) {
            Text(text = "Azimuth: ${uiState.azimuth?.roundToTwoDecimals()}")
            Text(text = "Distance: ${uiState.distance?.roundToTwoDecimals()} m")
            Text(text = "Elevation: ${uiState.elevation?.roundToTwoDecimals()}")
            Text(text = "isRangingPeerConnected: ${uiState.isRangingPeerConnected}")
            PositionArrow(
                uiState = uiState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PositionArrow(uiState: RangingUiState, modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.KeyboardArrowUp,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .size(200.dp)
            .rotate(uiState.azimuth ?: 0f)
    )
}

@Composable
private fun Float.roundToTwoDecimals(): String {
    val locale = LocalConfiguration.current.locales[0]
    return String.format(locale, "%.2f", this)
}

@Preview
@Composable
private fun RangingPreview() {
    AppTheme {
        RangingView(
            uiState = RangingUiState(
                distance = 0.5f,
                azimuth = 15.0f,
                elevation = 0.8f
            ),
            onBackClicked = {}
        )
    }
}
