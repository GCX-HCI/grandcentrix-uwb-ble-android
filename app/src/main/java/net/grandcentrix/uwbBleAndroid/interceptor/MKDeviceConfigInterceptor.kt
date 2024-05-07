package net.grandcentrix.uwbBleAndroid.interceptor

import net.grandcentrix.uwb.controlee.DeviceConfigInterceptor
import net.grandcentrix.uwbBleAndroid.model.MKDeviceConfig

object MKDeviceConfigInterceptor {
    fun intercept(): DeviceConfigInterceptor {
        val interceptor: DeviceConfigInterceptor = {
            MKDeviceConfig.fromByteArray(it)
        }
        return interceptor
    }
}
