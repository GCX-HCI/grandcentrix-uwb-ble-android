package net.grandcentrix.data.di

import androidx.core.uwb.UwbManager
import net.grandcentrix.ble.di.bleModule
import net.grandcentrix.data.manager.GcxUwbBleManager
import net.grandcentrix.data.manager.UwbBleManager
import org.koin.dsl.module

val dataModule = module {
    single<UwbManager> { UwbManager.createInstance(context = get()) }

    single<UwbBleManager> {
        GcxUwbBleManager(
            uwbManager = get(),
            bleManager = get(),
            bleScanner = get()
        )
    }
}

val provideSubModules = listOf(
    bleModule
)
