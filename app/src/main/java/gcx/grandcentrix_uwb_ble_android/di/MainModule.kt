package gcx.grandcentrix_uwb_ble_android.di

import gcx.grandcentrix_uwb_ble_android.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {
    viewModel {
        MainActivityViewModel(
            bleScanner = get(),
        )
    }
}