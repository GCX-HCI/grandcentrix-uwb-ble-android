package net.grandcentrix.uwbBleAndroid.di

import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import net.grandcentrix.uwbBleAndroid.ui.ble.BleViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        BleViewModel(
            uwbBleManager = get(),
            permissionChecker = get()
        )
    }

    single { PermissionChecker(context = get()) }
}
