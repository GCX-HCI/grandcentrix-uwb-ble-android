package net.grandcentrix.uwbBleAndroid.model

import net.grandcentrix.lib.uwb.ext.toByte
import net.grandcentrix.lib.uwb.ext.toInt
import net.grandcentrix.lib.uwb.ext.toShort
import net.grandcentrix.lib.uwb.model.DeviceConfig

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

    companion object {
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
}
