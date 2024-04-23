package net.grandcentrix.uwbBleAndroid

import android.app.Application
import net.grandcentrix.ble.di.bleModule
import net.grandcentrix.uwbBleAndroid.di.mainModule
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
