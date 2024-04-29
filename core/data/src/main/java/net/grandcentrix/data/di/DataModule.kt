package net.grandcentrix.data.di

import androidx.core.uwb.UwbManager
import net.grandcentrix.ble.di.bleModule
import net.grandcentrix.data.manager.GcxUwbBleLibrary
import net.grandcentrix.data.manager.UwbBleLibrary
import org.koin.dsl.module

val dataModule = module {
    single<UwbManager> { UwbManager.createInstance(context = get()) }

    single<UwbBleLibrary> {
        GcxUwbBleLibrary(
            uwbManager = get(),
            bleManager = get(),
            bleScanner = get()
        )
    }
}

val provideSubModules = listOf(
    bleModule
)
