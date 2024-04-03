package gcx.ble.di

import gcx.ble.manager.BleManager
import gcx.ble.manager.GcxBleManager
import gcx.ble.scanner.BleScanner
import gcx.ble.scanner.GcxBleScanner
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
