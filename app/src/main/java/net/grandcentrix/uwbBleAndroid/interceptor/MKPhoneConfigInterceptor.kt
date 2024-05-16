package net.grandcentrix.uwbBleAndroid.interceptor

import androidx.core.uwb.RangingParameters
import androidx.core.uwb.UwbComplexChannel
import net.grandcentrix.lib.uwb.controlee.PhoneConfigInterceptor
import net.grandcentrix.uwbBleAndroid.model.MKPhoneConfig

object MKPhoneConfigInterceptor : PhoneConfigInterceptor {
    override fun intercept(
        sessionId: Int,
        complexChannel: UwbComplexChannel,
        phoneAddress: ByteArray
    ): ByteArray = MKPhoneConfig(
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
