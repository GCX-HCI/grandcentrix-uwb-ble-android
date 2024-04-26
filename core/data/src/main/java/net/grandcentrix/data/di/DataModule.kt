package net.grandcentrix.data.di

import net.grandcentrix.ble.di.bleModule
import net.grandcentrix.data.manager.GcxUwbBleManager
import net.grandcentrix.data.manager.UwbBleManager
import org.koin.dsl.module

val dataModule = module {
    single<UwbBleManager> {
        GcxUwbBleManager(
            bleManager = get(),
            bleScanner = get()
        )
    }
}

val provideSubModules = listOf(
    bleModule
)
