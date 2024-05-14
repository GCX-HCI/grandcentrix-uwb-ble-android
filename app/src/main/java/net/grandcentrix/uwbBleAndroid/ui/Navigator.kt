package net.grandcentrix.uwbBleAndroid.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.grandcentrix.api.ble.model.GcxUwbDevice

class Navigator {

    private val _currentScreen =
        MutableStateFlow<Pair<Screen, GcxUwbDevice?>>(Screen.Connect to null)
    val currentScreen: StateFlow<Pair<Screen, GcxUwbDevice?>> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen, gcxUwbDevice: GcxUwbDevice? = null) {
        _currentScreen.update { screen to gcxUwbDevice }
    }
}
