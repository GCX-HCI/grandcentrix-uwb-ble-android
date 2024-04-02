package net.grandcentrix.scaffold.app

import android.app.Application
import net.grandcentrix.scaffold.app.di.authModule
import net.grandcentrix.scaffold.app.di.pushModule
import net.grandcentrix.scaffold.app.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Use application context whenever Context is injected
            androidContext(applicationContext)
            modules(
                authModule,
                uiModule,
                pushModule
            )
        }
    }
}
