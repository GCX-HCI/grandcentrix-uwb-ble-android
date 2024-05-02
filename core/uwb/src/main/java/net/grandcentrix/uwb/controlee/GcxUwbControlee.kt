package net.grandcentrix.uwb.controlee

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.uwb.RangingParameters
import androidx.core.uwb.RangingResult
import androidx.core.uwb.UwbComplexChannel
import androidx.core.uwb.UwbControleeSessionScope
import androidx.core.uwb.UwbDevice
import androidx.core.uwb.UwbManager
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleMessagingClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.model.BluetoothMessage
import net.grandcentrix.ble.protocol.OOBMessageProtocol
import net.grandcentrix.uwb.ext.hexStringToByteArray
import net.grandcentrix.uwb.model.DeviceConfig
import net.grandcentrix.uwb.model.MKDeviceConfig
import net.grandcentrix.uwb.model.MKPhoneConfig

private const val TAG = "GcxUwbControlee"

interface UwbControlee {
    fun startRanging(): Flow<RangingResult>
}

@SuppressLint("MissingPermission")
class GcxUwbControlee(
    private val uwbManager: UwbManager,
    private val bleMessages: SharedFlow<BluetoothMessage>,
    private val bleMessagingClient: BleMessagingClient
) : UwbControlee {

    private lateinit var uwbControleeSession: UwbControleeSessionScope
    private val sessionId = Random.Default.nextInt()
    private val sessionFlow: MutableSharedFlow<RangingResult> = MutableSharedFlow()

    private val uwbComplexChannel = UwbComplexChannel(channel = 9, preambleIndex = 10)

    override fun startRanging(): Flow<RangingResult> = channelFlow {
        launch { collectBleMessages() }

        bleMessagingClient.enableReceiver()

        launch {
            bleMessagingClient.send(
                byteArrayOf(OOBMessageProtocol.INITIALIZE.command)
            )
        }

        sessionFlow.collect { send(it) }
    }

    private suspend fun transmitPhoneData(): Result<BluetoothMessage> {
        uwbControleeSession = uwbManager.controleeSessionScope()
        val localAddress = uwbControleeSession.localAddress

        val phoneConfig = MKPhoneConfig(
            specVerMajor = 0x0100.toShort(),
            specVerMinor = 0x0000.toShort(),
            sessionId = sessionId,
            preambleIndex = uwbComplexChannel.preambleIndex.toByte(),
            channel = uwbComplexChannel.channel.toByte(),
            profileId = RangingParameters.CONFIG_UNICAST_DS_TWR.toByte(),
            deviceRangingRole = 0x01.toByte(),
            phoneAddress = localAddress.address
        )

        return bleMessagingClient.send(
            byteArrayOf(
                OOBMessageProtocol.UWB_PHONE_CONFIG_DATA.command
            ) + phoneConfig.toByteArray()
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
            updateRateType = RangingParameters.RANGING_UPDATE_RATE_FREQUENT
        )

        sessionFlow.emitAll(uwbControleeSession.prepareSession(partnerParameters))
    }

    private suspend fun collectBleMessages() {
        bleMessages
            .filter { it.uuid.toString() == GcxBleManager.UART_TX_CHARACTERISTIC }
            .collect {
                it.data?.let { bytes ->
                    when (bytes.first()) {
                        OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command -> {
                            val deviceConfig = MKDeviceConfig.fromByteArray(bytes)
                            transmitPhoneData()
                                .onSuccess {
                                    startSession(deviceConfig = deviceConfig)
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
