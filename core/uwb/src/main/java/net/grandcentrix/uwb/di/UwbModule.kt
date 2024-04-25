package net.grandcentrix.uwb.di

import kotlinx.coroutines.channels.Channel
import net.grandcentrix.ble.manager.BleClient
import net.grandcentrix.ble.model.BluetoothResult
import net.grandcentrix.uwb.controlee.GcxUwbControlee
import org.koin.dsl.module

val uwbModule = module {
    single {
            (
                channel: Channel<BluetoothResult>,
                client: BleClient
            ) ->
        GcxUwbControlee(
            context = get(),
            receiveChannel = channel,
            bleClient = client
        )
    }
}
