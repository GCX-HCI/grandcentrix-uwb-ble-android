package net.grandcentrix.uwb.model

import net.grandcentrix.uwb.ext.toByteArray

class MKPhoneConfig(
    val specVerMajor: Short,
    val specVerMinor: Short,
    override val sessionId: Int,
    override val preambleIndex: Byte,
    override val channel: Byte,
    val profileId: Byte,
    var deviceRangingRole: Byte,
    override val phoneAddress: ByteArray
) : PhoneConfig(
    sessionId = sessionId,
    preambleIndex = preambleIndex,
    channel = channel,
    phoneAddress = phoneAddress
) {
    fun toByteArray(): ByteArray {
        return this.specVerMajor.toByteArray() +
            this.specVerMinor.toByteArray() +
            this.sessionId.toByteArray() +
            this.preambleIndex.toByteArray() +
            this.channel.toByteArray() +
            this.profileId.toByteArray() +
            this.deviceRangingRole.toByteArray() +
            this.phoneAddress
    }
}
