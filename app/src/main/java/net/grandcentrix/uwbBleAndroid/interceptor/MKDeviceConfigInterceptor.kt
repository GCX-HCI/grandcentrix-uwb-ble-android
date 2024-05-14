package net.grandcentrix.uwbBleAndroid.interceptor

import net.grandcentrix.api.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.api.uwb.model.DeviceConfig
import net.grandcentrix.uwbBleAndroid.model.MKDeviceConfig

object MKDeviceConfigInterceptor : DeviceConfigInterceptor {
    override fun intercept(byteArray: ByteArray): DeviceConfig =
        MKDeviceConfig.fromByteArray(byteArray)
}
