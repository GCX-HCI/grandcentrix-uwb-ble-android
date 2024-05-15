package net.grandcentrix.uwbBleAndroid.di

import net.grandcentrix.api.ble.model.GcxUwbDevice
import net.grandcentrix.api.data.manager.GcxUwbBleLibrary
import net.grandcentrix.api.data.manager.UwbBleLibrary
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import net.grandcentrix.uwbBleAndroid.ui.Navigator
import net.grandcentrix.uwbBleAndroid.ui.ble.BleViewModel
import net.grandcentrix.uwbBleAndroid.ui.ranging.RangingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        BleViewModel(
            uwbBleLibrary = get(),
            permissionChecker = get(),
            navigator = get()
        )
    }

    viewModel { (uwbDevice: GcxUwbDevice) ->
        RangingViewModel(
            gcxUwbDevice = uwbDevice,
            navigator = get(),
            permissionChecker = get()
        )
    }

    single<UwbBleLibrary> {
        GcxUwbBleLibrary(context = get())
    }

    single { PermissionChecker(context = get()) }
    single { Navigator() }
}
