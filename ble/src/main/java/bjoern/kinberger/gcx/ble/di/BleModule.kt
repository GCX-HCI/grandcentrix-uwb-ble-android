package bjoern.kinberger.gcx.ble.di

import bjoern.kinberger.gcx.ble.manager.BleManager
import bjoern.kinberger.gcx.ble.manager.GcxBleManager
import bjoern.kinberger.gcx.ble.scanner.BleScanner
import bjoern.kinberger.gcx.ble.scanner.GcxBleScanner
import org.koin.dsl.module

val bleModule =
    module {
        single<BleManager> {
            GcxBleManager(
                context = get(),
            )
        }

        single<BleScanner> {
            GcxBleScanner(
                bleManager = get(),
            )
        }
    }
