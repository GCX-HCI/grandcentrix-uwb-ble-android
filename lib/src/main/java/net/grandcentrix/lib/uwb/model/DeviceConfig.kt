package net.grandcentrix.lib.uwb.model

/**
 * This open class serves as a base configuration for uwb devices that function
 * as a controller and can help to setup the DeviceConfigInterceptor.
 *
 * @property deviceMacAddress The MAC address of the device in byte array format.
 */
open class DeviceConfig(
    open val deviceMacAddress: ByteArray
)
