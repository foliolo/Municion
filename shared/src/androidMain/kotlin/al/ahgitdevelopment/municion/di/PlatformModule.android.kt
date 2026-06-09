package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.data.local.room.MUNICION_DATABASE_NAME
import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.sync.AndroidSyncScheduler
import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // Room database builder over the app's databases dir (reuses the existing municion.db).
    single<RoomDatabase.Builder<MunicionDatabase>> {
        val context: Context = androidContext()
        val dbFile = context.getDatabasePath(MUNICION_DATABASE_NAME)
        Room.databaseBuilder<MunicionDatabase>(context, dbFile.absolutePath)
    }

    // Sync scheduling via WorkManager.
    single<SyncScheduler> { AndroidSyncScheduler(androidContext()) }
}
