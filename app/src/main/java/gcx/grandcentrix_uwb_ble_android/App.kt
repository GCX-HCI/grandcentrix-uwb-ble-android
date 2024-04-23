package gcx.grandcentrix_uwb_ble_android

import android.app.Application
import gcx.ble.di.bleModule
import gcx.grandcentrix_uwb_ble_android.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(applicationContext)

            modules(
                listOf(
                    mainModule,
                    bleModule
                )
            )
        }
    }
}
