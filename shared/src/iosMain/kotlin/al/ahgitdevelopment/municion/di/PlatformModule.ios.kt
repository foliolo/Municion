@file:OptIn(ExperimentalForeignApi::class)

package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.ads.IosNativeAdManager
import al.ahgitdevelopment.municion.ads.NativeAdManager
import al.ahgitdevelopment.municion.auth.IosSocialAuthProvider
import al.ahgitdevelopment.municion.auth.SocialAuthProvider
import al.ahgitdevelopment.municion.data.local.room.MUNICION_DATABASE_NAME
import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.sync.IosSyncScheduler
import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.platform.CalendarManager
import al.ahgitdevelopment.municion.platform.IosCalendarManager
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule: Module =
    module {
        // Room database builder over NSDocumentDirectory (the app sandbox).
        single<RoomDatabase.Builder<MunicionDatabase>> {
            val documentsDir =
                NSFileManager.defaultManager.URLForDirectory(
                    directory = NSDocumentDirectory,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                )
            val dbPath = requireNotNull(documentsDir?.path) + "/" + MUNICION_DATABASE_NAME
            Room.databaseBuilder<MunicionDatabase>(name = dbPath)
        }

        // Sync scheduling: foreground + on-demand drains.
        single<SyncScheduler> { IosSyncScheduler(get(), get()) }

        // Calendar reminders via EventKit.
        single<CalendarManager> { IosCalendarManager(get()) }

        // Native advanced ads pool (delegates to the Swift bridge).
        single<NativeAdManager> { IosNativeAdManager(get()) }

        // Firebase Auth social providers.
        single<SocialAuthProvider> { IosSocialAuthProvider(get(), get()) }
    }
