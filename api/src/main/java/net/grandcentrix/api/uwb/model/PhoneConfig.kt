package net.grandcentrix.uwb.model

open class PhoneConfig(
    open val sessionId: Int,
    open val preambleIndex: Byte,
    open val channel: Byte,
    open val phoneAddress: ByteArray
)
