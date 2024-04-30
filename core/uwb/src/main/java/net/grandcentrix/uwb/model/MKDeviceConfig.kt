package net.grandcentrix.uwb.model

import net.grandcentrix.uwb.ext.toByte
import net.grandcentrix.uwb.ext.toInt
import net.grandcentrix.uwb.ext.toShort

class MKDeviceConfig(
    val specVerMajor: Short,
    val specVerMinor: Short,
    val chipId: ByteArray,
    val chipFwVersion: ByteArray,
    val mwVersion: ByteArray,
    val supportedUwbProfileIds: Int,
    val supportedDeviceRangingRoles: Byte,
    override val deviceMacAddress: ByteArray
) : DeviceConfig(
    deviceMacAddress = deviceMacAddress
) {
    fun fromByteArray(data: ByteArray) = MKDeviceConfig(
        specVerMajor = data.sliceArray(0..1).toShort(),
        specVerMinor = data.sliceArray(2..3).toShort(),
        chipId = data.sliceArray(4..5),
        chipFwVersion = data.sliceArray(6..7),
        mwVersion = data.sliceArray(8..10),
        supportedUwbProfileIds = data.sliceArray(11..14).toInt(),
        supportedDeviceRangingRoles = data.sliceArray(15..15).toByte(),
        deviceMacAddress = data.sliceArray(17..18)
    )
}
