package net.grandcentrix.scaffold.app.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import net.grandcentrix.scaffold.app.data.push.PushRepository
import net.grandcentrix.scaffold.app.framework.push.DataStorePushService
import net.grandcentrix.scaffold.app.framework.push.GcxFirebaseMessagingService
import net.grandcentrix.scaffold.app.usecases.push.GetPushTokenUseCase
import net.grandcentrix.scaffold.app.usecases.push.OnNewPushTokenUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val QUALIFIER_SETTINGS = "settings"

val pushModule = module {

    single(named(QUALIFIER_SETTINGS)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(name = QUALIFIER_SETTINGS)
        }
    }
    single<PushRepository> { DataStorePushService(dataStore = get(named(QUALIFIER_SETTINGS))) }

    single { OnNewPushTokenUseCase(repository = get()) }
    single { GetPushTokenUseCase(repository = get()) }

    single { GcxFirebaseMessagingService() }
}
