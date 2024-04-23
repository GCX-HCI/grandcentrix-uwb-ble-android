package net.grandcentrix.uwbBleAndroid.di

import net.grandcentrix.uwbBleAndroid.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        MainActivityViewModel(
            bleScanner = get(),
            bleManager = get()
        )
    }
}
