package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.logger.CrashReportingTree
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
open class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Setup Timber
        Timber.plant(
            if (BuildConfig.DEBUG) {
                Timber.DebugTree()
            } else {
                CrashReportingTree()
            }
        )

        // Setup UncaughtException
        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            Timber.e(paramThrowable, "Uncaught Exception")
        }
    }
}
