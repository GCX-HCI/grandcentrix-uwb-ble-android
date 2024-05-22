package net.grandcentrix.lib.uwb.model

/**
 * This open class serves as a base configuration for a phone that function as a controlee.
 *
 * @property sessionId The session identifier for the ranging operation (integer).
 * @property preambleIndex The preamble index used for transmission (byte).
 * @property channel The channel to be used for communication (byte).
 * @property phoneAddress The phone's address in byte array format.
 */

open class PhoneConfig(
    open val sessionId: Int,
    open val preambleIndex: Byte,
    open val channel: Byte,
    open val phoneAddress: ByteArray
)
