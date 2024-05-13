package net.grandcentrix.uwbBleAndroid

import android.app.Application
import net.grandcentrix.uwb.ext.hexStringToByteArray
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
                    mainModule
                )
            )
        }
    }

    companion object {
        val MK_UWB_SESSION_KEY = "0807010203040506".hexStringToByteArray()
    }
}
