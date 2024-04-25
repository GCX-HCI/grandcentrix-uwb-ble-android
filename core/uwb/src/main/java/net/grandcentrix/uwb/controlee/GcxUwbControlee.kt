package net.grandcentrix.uwb.controlee

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.uwb.UwbManager
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.model.BluetoothResult
import net.grandcentrix.ble.protocol.OOBMessageProtocol

private const val TAG = "GcxUwbControlee"

interface UwbControlee {
    fun requestConfigData(
        data: ByteArray = byteArrayOf(OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command)
    )
}

@SuppressLint("MissingPermission")
class GcxUwbControlee(
    context: Context,
    coroutineContext: CoroutineContext = Dispatchers.IO,
    private val receiveChannel: Channel<BluetoothResult>,
    private val bleClient: BleClient
) : UwbControlee {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    val uwbManager: UwbManager = UwbManager.createInstance(context)

    override fun requestConfigData(data: ByteArray) {
        scope.launch {
            bleClient.send(data)
        }
    }

    init {
        scope.launch {
            receiveChannel.consumeAsFlow().collect {
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
}
