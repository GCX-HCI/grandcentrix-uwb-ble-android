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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleMessagingClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.model.BluetoothMessage
import net.grandcentrix.ble.protocol.OOBMessageProtocol
import net.grandcentrix.uwb.exception.UwbException
import net.grandcentrix.uwb.ext.hexStringToByteArray
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
    private val sessionFlow: MutableSharedFlow<RangingResult> = MutableSharedFlow()

    private val uwbComplexChannel = UwbComplexChannel(channel = 9, preambleIndex = 10)

    @RequiresPermission(
        allOf = [Manifest.permission.UWB_RANGING, Manifest.permission.BLUETOOTH_CONNECT]
    )
    override fun startRanging(): Flow<RangingResult> = channelFlow {
        launch { collectBleMessages() }

        bleMessagingClient.enableReceiver()

        launch {
            bleMessagingClient.send(byteArrayOf(OOBMessageProtocol.INITIALIZE.command))
                .onFailure { close(UwbException.InitialisationFailure) }
        }

        launch { sessionFlow.collect { send(it) } }

        awaitClose {
            // TODO: send stop ranging command
            Log.d(TAG, "close ranging")
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun transmitPhoneData(): Result<Unit> {
        uwbControleeSession = uwbManager.controleeSessionScope()
        val localAddress = uwbControleeSession.localAddress

        return bleMessagingClient.send(
            byteArrayOf(
                OOBMessageProtocol.UWB_PHONE_CONFIG_DATA.command
            ) + phoneConfigInterceptor.intercept(
                sessionId = sessionId,
                complexChannel = uwbComplexChannel,
                phoneAddress = localAddress.address
            )
        )
    }

    private suspend fun startSession(deviceConfig: DeviceConfig) {
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
        sessionFlow.emitAll(uwbControleeSession.prepareSession(partnerParameters))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun collectBleMessages() {
        coroutineScope {
            launch {
                bleMessagingClient.messages
                    .filter { it.uuid.toString() == GcxBleManager.UART_TX_CHARACTERISTIC }
                    .collect {
                        it.data?.let { bytes ->
                            when (bytes.first()) {
                                OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command -> {
                                    val deviceConfig = deviceConfigInterceptor.intercept(bytes)
                                    transmitPhoneData()
                                        .onSuccess {
                                            launch {
                                                startSession(deviceConfig = deviceConfig)
                                            }
                                        }
                                }

                                OOBMessageProtocol.UWB_DID_START.command -> {
                                    Log.d(TAG, "UWB started")
                                }

                                else -> {
                                    Log.e(TAG, "Unknown message id")
                                }
                            }
                        }
                    }
            }
        }
    }
}
