package net.grandcentrix.uwb.controlee

import android.annotation.SuppressLint
import android.util.Log
import androidx.core.uwb.UwbManager
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.grandcentrix.ble.manager.BleClient
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.model.BluetoothMessage
import net.grandcentrix.ble.protocol.OOBMessageProtocol

private const val TAG = "GcxUwbControlee"

interface UwbControlee {
    fun startRanging()
}

@SuppressLint("MissingPermission")
class GcxUwbControlee(
    coroutineContext: CoroutineContext = Dispatchers.IO,
    private val uwbManager: UwbManager,
    private val resultChannel: SharedFlow<BluetoothMessage>,
    private val bleClient: BleClient
) : UwbControlee {

    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    override fun startRanging() {
        bleClient.enableReceiver()
        scope.launch {
            bleClient.send(byteArrayOf(OOBMessageProtocol.UWB_DEVICE_CONFIG_DATA.command))
        }
    }

    init {
        scope.launch {
            resultChannel.collect {
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
