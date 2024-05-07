package net.grandcentrix.uwbBleAndroid.interceptor

import androidx.core.uwb.RangingParameters
import net.grandcentrix.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.uwbBleAndroid.model.MKPhoneConfig

object MKPhoneConfigInterceptor {
    fun intercept(): PhoneConfigInterceptor {
        val interceptor: PhoneConfigInterceptor = { sessionId, complexChannel, phoneAddress ->
            MKPhoneConfig(
                specVerMajor = 0x0100.toShort(),
                specVerMinor = 0x0000.toShort(),
                sessionId = sessionId,
                preambleIndex = complexChannel.preambleIndex.toByte(),
                channel = complexChannel.channel.toByte(),
                profileId = RangingParameters.CONFIG_UNICAST_DS_TWR.toByte(),
                deviceRangingRole = 0x01.toByte(),
                phoneAddress = phoneAddress
            ).toByteArray()
        }
        return interceptor
    }
}
