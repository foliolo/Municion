package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.di.initKoin
import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.mp.KoinPlatform

/**
 * Application entry point. Starts Koin with the Android [Context] and kicks off sync scheduling.
 * App Check and AdMob/consent init are wired in later migration phases.
 */
class MunicionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MunicionApplication)
        }
        // Periodic outbox drain + daily tombstone cleanup + startup recovery.
        KoinPlatform.getKoin().get<SyncScheduler>().start()
    }
}
