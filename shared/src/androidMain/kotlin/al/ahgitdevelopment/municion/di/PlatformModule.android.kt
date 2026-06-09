package al.ahgitdevelopment.municion.di

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Android bindings (Room builder, multiplatform-settings, SyncScheduler, calendar,
    // camera, ads) are added in later phases. Context is provided via androidContext()
    // in MunicionApplication.initKoin { androidContext(...) }.
}
