package net.grandcentrix.uwbBleAndroid

import android.app.Application
import net.grandcentrix.api.uwb.ext.hexStringToByteArray
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
        /**
         * Represents the default session key for the MK UWB dev kit. SessionKey is used to match Vendor ID in UWB Device firmware
         * The session key is represented as a hexadecimal string "0807010203040506" and converted to a byte array.
         */
        val MK_UWB_SESSION_KEY = "0807010203040506".hexStringToByteArray()
    }
}
