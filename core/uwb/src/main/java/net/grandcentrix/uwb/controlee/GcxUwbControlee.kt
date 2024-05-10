package net.grandcentrix.uwb.controlee

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleMessagingClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.protocol.OOBMessageProtocol
import net.grandcentrix.uwb.exception.UwbException
import net.grandcentrix.uwb.ext.hexStringToByteArray
import net.grandcentrix.uwb.ext.toHexString
import net.grandcentrix.uwb.model.DeviceConfig

private const val TAG = "GcxUwbControlee"

/**
 * Interface for intercepting and interpreting device configuration data received from the controller device.
 *
 * This interface allows developers to create custom interceptors to handle byte data received as device configuration
 * and map it to their own data model.
 */
interface DeviceConfigInterceptor {

    /**
     * Intercepts and interprets the byte array representing device configuration data.
     *
     * This method is called when device configuration data is received. Developers can implement their
     * own logic to parse and interpret the byte array into a custom [DeviceConfig] model.
     *
     * @param byteArray The byte array representing the device configuration data received from the controller device.
     * @return A [DeviceConfig] object representing the interpreted device configuration data.
     */
    fun intercept(byteArray: ByteArray): DeviceConfig
}

/**
 * Interface for intercepting and customizing phone configuration data generated on the controlee side.
 *
 * This interface provides a mechanism for developers to intercept and modify the phone configuration data
 * before it is sent. Developers can add extra parameters to the byte array as needed while ensuring that
 * the required variables for a UWB session are already declared by the interface.
 */
interface PhoneConfigInterceptor {

    /**
     * Intercepts and customizes the phone configuration data before transmission.
     *
     * This method is called when generating phone configuration data on the controlee side. Developers
     * can implement their own logic to customize the configuration data based on the provided parameters.
     *
     * @param sessionId The session ID associated with the UWB session.
     * @param complexChannel The UWB complex channel used for communication.
     * @param phoneAddress The byte array representing the address of the phone.
     * @return A modified byte array representing the customized phone configuration data.
     */
    fun intercept(
        sessionId: Int,
        complexChannel: UwbComplexChannel,
        phoneAddress: ByteArray
    ): ByteArray
}

interface UwbControlee {
    @RequiresPermission(Manifest.permission.UWB_RANGING)
    fun startRanging(): Flow<RangingResult>
}

class GcxUwbControlee(
    private val uwbManager: UwbManager,
    private val bleMessagingClient: BleMessagingClient,
    private val deviceConfigInterceptor: DeviceConfigInterceptor,
    private val phoneConfigInterceptor: PhoneConfigInterceptor
) : UwbControlee {

    private lateinit var uwbControleeSession: UwbControleeSessionScope
    private val sessionId = Random.Default.nextInt()

    private val uwbComplexChannel = UwbComplexChannel(channel = 9, preambleIndex = 10)

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    override fun startRanging(): Flow<RangingResult> = flow {
        Log.i(TAG, "Start UWB ranging")
        bleMessagingClient.enableReceiver()
        val deviceConfig = coroutineScope { requestDeviceConfig().await() }
        transmitPhoneData().getOrThrow()
        emitAll(startSession(deviceConfig))
    }.onCompletion {
        // TODO: send stop ranging command
        Log.d(TAG, "Close UWB ranging")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun CoroutineScope.requestDeviceConfig(): Deferred<DeviceConfig> {
        val deviceConfigDeferred = async { getDeviceConfigDataOrNull() }
        launch { transmitInitializeCommand().getOrThrow() }
        return deviceConfigDeferred
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun transmitInitializeCommand(): Result<Unit> {
        val initializeCommandBytes = byteArrayOf(OOBMessageProtocol.INITIALIZE.command)
        Log.i(
            TAG,
            "Sending initialize command to uwb device: ${initializeCommandBytes.toHexString()}"
        )
        return bleMessagingClient.send(initializeCommandBytes)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun transmitPhoneData(): Result<Unit> {
        uwbControleeSession = uwbManager.controleeSessionScope()
        val localAddress = uwbControleeSession.localAddress

        val phoneConfigBytes = byteArrayOf(
            OOBMessageProtocol.UWB_PHONE_CONFIG_DATA.command
        ) + phoneConfigInterceptor.intercept(
            sessionId = sessionId,
            complexChannel = uwbComplexChannel,
            phoneAddress = localAddress.address
        )
        Log.i(TAG, "Sending phone data to uwb device: ${phoneConfigBytes.toHexString()}")
        return bleMessagingClient.send(phoneConfigBytes)
    }

    private fun startSession(deviceConfig: DeviceConfig): Flow<RangingResult> {
        val uwbDevice = UwbDevice.createForAddress(deviceConfig.deviceMacAddress)

        // https://developer.android.com/guide/topics/connectivity/uwb#known_issue_byte_order_reversed_for_mac_address_and_static_sts_vendor_id_fields
        // GMS Core update is doing byte reverse as per UCI spec
        // SessionKey is used to match Vendor ID in UWB Device firmware
        val sessionKey: ByteArray = "0807010203040506".hexStringToByteArray()

        val partnerParameters = RangingParameters(
            uwbConfigType = RangingParameters.CONFIG_UNICAST_DS_TWR,
            sessionId = sessionId,
            sessionKeyInfo = sessionKey,
            complexChannel = uwbComplexChannel,
            peerDevices = listOf(uwbDevice),
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT,
            subSessionId = 0,
            subSessionKeyInfo = null
        )
        return uwbControleeSession.prepareSession(partnerParameters)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun getDeviceConfigDataOrNull(): DeviceConfig {
        return bleMessagingClient.messages
            .filter { it.uuid.toString() == GcxBleManager.UART_TX_CHARACTERISTIC }
            .filter { it.data?.first() == OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command }
            .map { it.data?.let { bytes -> deviceConfigInterceptor.intercept(bytes) } }
            .firstOrNull() ?: throw UwbException.DeciveConfigNullException
    }
}
