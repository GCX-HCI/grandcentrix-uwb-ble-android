package net.grandcentrix.ble.di

import net.grandcentrix.ble.manager.BleManager
import net.grandcentrix.ble.manager.GcxBleManager
import net.grandcentrix.ble.scanner.BleScanner
import net.grandcentrix.ble.scanner.GcxBleScanner
import org.koin.dsl.module

val bleModule = module {
    single<BleManager> {
        GcxBleManager(
            context = get()
        )
    }

    single<BleScanner> {
        GcxBleScanner(
            bleManager = get()
        )
    }
}
