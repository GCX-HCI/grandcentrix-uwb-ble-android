package net.grandcentrix.uwbBleAndroid.ui

import net.grandcentrix.api.ble.model.GcxUwbDevice

sealed interface Screen {
    data object Connect : Screen

    data class Ranging(val uwbDevice: GcxUwbDevice) : Screen
}
