package net.grandcentrix.api.ble.model

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.api.ble.manager.BleMessagingClient
import net.grandcentrix.api.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.api.uwb.controlee.GcxUwbControlee
import net.grandcentrix.api.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.api.uwb.controlee.UwbControlee
import net.grandcentrix.api.uwb.model.RangingConfig

class GcxUwbDevice(
    private val bleMessagingClient: BleMessagingClient,
    private val context: Context
) {
    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    fun startRanging(
        deviceConfigInterceptor: DeviceConfigInterceptor,
        phoneConfigInterceptor: PhoneConfigInterceptor,
        rangingConfig: RangingConfig
    ): Flow<RangingResult> {
        val uwbControlee: UwbControlee = GcxUwbControlee(
            uwbManager = UwbManager.createInstance(context = context),
            bleMessagingClient = bleMessagingClient
        )
        return uwbControlee.startRanging(
            deviceConfigInterceptor = deviceConfigInterceptor,
            phoneConfigInterceptor = phoneConfigInterceptor,
            rangingConfig = rangingConfig
        )
    }
}
