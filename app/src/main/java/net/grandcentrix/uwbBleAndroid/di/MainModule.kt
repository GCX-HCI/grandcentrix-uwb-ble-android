package net.grandcentrix.uwbBleAndroid.di

import net.grandcentrix.data.manager.GcxUwbBleLibrary
import net.grandcentrix.data.manager.UwbBleLibrary
import net.grandcentrix.uwb.model.RangingConfig
import net.grandcentrix.uwbBleAndroid.App
import net.grandcentrix.uwbBleAndroid.interceptor.MKDeviceConfigInterceptor
import net.grandcentrix.uwbBleAndroid.interceptor.MKPhoneConfigInterceptor
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

    single<UwbBleLibrary> {
        GcxUwbBleLibrary(
            context = get(),
            deviceConfigInterceptor = MKDeviceConfigInterceptor,
            phoneConfigInterceptor = MKPhoneConfigInterceptor,
            rangingConfig = RangingConfig(sessionKey = App.MK_UWB_SESSION_KEY)
        )
    }

    single { PermissionChecker(context = get()) }
    single { Navigator() }
}
