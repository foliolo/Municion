package al.ahgitdevelopment.municion.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // iOS bindings (Room builder over NSDocumentDirectory, NSUserDefaults settings,
    // foreground SyncScheduler, EventKit, image capture) are added in later phases.
}
