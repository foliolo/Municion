package al.ahgitdevelopment.municion.di

import org.koin.core.module.Module

/** Platform-specific bindings (Room builder, settings, schedulers, platform services). */
expect val platformModule: Module
