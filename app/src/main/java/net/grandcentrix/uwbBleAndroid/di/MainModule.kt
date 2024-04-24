package net.grandcentrix.uwbBleAndroid.di

import net.grandcentrix.uwbBleAndroid.MainActivityViewModel
import net.grandcentrix.uwbBleAndroid.permission.PermissionChecker
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        MainActivityViewModel(
            bleScanner = get(),
            bleManager = get(),
            permissionChecker = get()
        )
    }

    single { PermissionChecker(context = get()) }
}
