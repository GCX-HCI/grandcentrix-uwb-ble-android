package net.grandcentrix.uwbBleAndroid.permission

import android.Manifest

object AppPermissions {
    val uwbRangingPermissions = listOf(
        Manifest.permission.UWB_RANGING
    )

    val bleScanPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN
    )

    val bleConnectPermissions = listOf(
        Manifest.permission.BLUETOOTH_CONNECT
    )
}
