package net.grandcentrix.lib.ble.model

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.flow.Flow
import net.grandcentrix.lib.ble.gatt.BleMessagingClient
import net.grandcentrix.lib.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.lib.uwb.controlee.GcxUwbControlee
import net.grandcentrix.lib.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.lib.uwb.controlee.UwbControlee
import net.grandcentrix.lib.uwb.model.RangingConfig
import net.grandcentrix.lib.uwb.model.UwbResult

/**
 * This class provides functionalities for initiating UWB ranging with a bluetooth device.
 */
class GcxUwbDevice(
    private val bleMessagingClient: BleMessagingClient,
    private val context: Context
) {

    /**
     * Starts UWB ranging with the provided configurations.
     *
     * This function creates a controlee instance and returns a [Flow] that emits [UwbResult] objects.
     *
     * @param deviceConfigInterceptor interceptor to modify device configuration package, see [DeviceConfigInterceptor]
     * @param phoneConfigInterceptor interceptor to modify phone configuration package, see [PhoneConfigInterceptor]
     * @param rangingConfig config object to set the parameters for a session, see [RangingConfig]
     * @return A Flow of [UwbResult] objects containing ranging data or errors.
     */
    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    fun startRanging(
        deviceConfigInterceptor: DeviceConfigInterceptor,
        phoneConfigInterceptor: PhoneConfigInterceptor,
        rangingConfig: RangingConfig
    ): Flow<UwbResult> {
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
