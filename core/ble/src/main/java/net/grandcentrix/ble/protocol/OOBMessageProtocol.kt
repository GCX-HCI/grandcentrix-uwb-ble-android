package net.grandcentrix.ble.protocol

enum class OOBMessageProtocol(val command: Byte) {
    // Messages from the UWB controller
    UWB_DEVICE_CONFIG_DATA(0x01),

    // Messages from the UWB controlee
    INITIALIZE(0xA5.toByte())
}
