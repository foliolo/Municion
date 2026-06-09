package al.ahgitdevelopment.municion

import android.app.Application
import al.ahgitdevelopment.municion.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Application entry point.
 *
 * Starts Koin with the Android [Context]. Firebase RTDB persistence, App Check, AdMob
 * (consent), the WorkManager [androidx.work.Configuration.Provider] and the sync scheduler
 * are wired here in later migration phases.
 */
class MunicionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@MunicionApplication)
        }
    }
}
