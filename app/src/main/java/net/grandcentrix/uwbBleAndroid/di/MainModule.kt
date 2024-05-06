package net.grandcentrix.uwbBleAndroid.di

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

    viewModel {
        RangingViewModel(
            uwbBleLibrary = get(),
            navigator = get(),
            permissionChecker = get()
        )
    }

    single { PermissionChecker(context = get()) }
    single { Navigator() }
}
