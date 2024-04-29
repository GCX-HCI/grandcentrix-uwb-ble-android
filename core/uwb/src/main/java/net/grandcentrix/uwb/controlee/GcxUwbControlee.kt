package net.grandcentrix.uwb.controlee

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.uwb.UwbManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleMessagingClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.model.BluetoothMessage
import net.grandcentrix.ble.protocol.OOBMessageProtocol

private const val TAG = "GcxUwbControlee"

interface UwbControlee {
    suspend fun startRanging()
}

@SuppressLint("MissingPermission")
class GcxUwbControlee(
    private val uwbManager: UwbManager,
    private val bleMessages: SharedFlow<BluetoothMessage>,
    private val bleMessagingClient: BleMessagingClient
) : UwbControlee {

    override suspend fun startRanging() {
        coroutineScope {
            launch {
                collectBleMessages()
            }

            launch {
                bleMessagingClient.enableReceiver()
            }

            launch {
                bleMessagingClient.send(
                    byteArrayOf(OOBMessageProtocol.INITIALIZE.command)
                )
            }
        }
    }

    private suspend fun collectBleMessages() {
        bleMessages.collect {
            if (it.uuid.toString() == GcxBleManager.UART_TX_CHARACTERISTIC) {
                it.data?.let { bytes ->
                    when (bytes.first()) {
                        OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command -> {
                            Log.d(TAG, "config data ${bytes.contentToString()}")
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
