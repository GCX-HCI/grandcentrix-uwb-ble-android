package net.grandcentrix.uwbBleAndroid.interceptor

import net.grandcentrix.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.uwb.model.DeviceConfig
import net.grandcentrix.uwbBleAndroid.model.MKDeviceConfig

object MKDeviceConfigInterceptor : DeviceConfigInterceptor {
    override fun intercept(byteArray: ByteArray): DeviceConfig =
        MKDeviceConfig.fromByteArray(byteArray)
}
