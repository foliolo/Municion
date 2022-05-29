package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.logger.CrashReportingTree
import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
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

        // Setup admob
        if (BuildConfig.DEBUG) {
            resources.getStringArray(R.array.admob_test_devices).toList().let { testDevices ->
                RequestConfiguration.Builder().setTestDeviceIds(testDevices).build().let {
                    MobileAds.setRequestConfiguration(it)
                }
            }
        }
        MobileAds.initialize(this)

        // Setup UncaughtException
        Thread.setDefaultUncaughtExceptionHandler { _, paramThrowable ->
            Timber.e(paramThrowable, "Uncaught Exception")
        }
    }
}
