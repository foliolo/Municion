package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.data.sync.SyncScheduler
import al.ahgitdevelopment.municion.di.initKoin
import android.app.Application
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.manualFileKitCoreInitialization
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.mp.KoinPlatform

/**
 * Application entry point. Starts Koin with the Android [Context] and kicks off sync scheduling.
 * App Check is installed per build variant (see AppCheckInstaller in src/{debug,release});
 * AdMob/consent init are wired in later migration phases.
 */
class MunicionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // App Check must be installed before any Firebase backend call: Auth, Realtime Database
        // and Storage are enforced, so requests without a valid token are rejected.
        installAppCheck()

        // FileKit Core (reads picked image bytes) needs manual init when App Startup is constrained.
        FileKit.manualFileKitCoreInitialization(this)
        initKoin {
            androidLogger()
            androidContext(this@MunicionApplication)
        }
        // Periodic outbox drain + daily tombstone cleanup + startup recovery.
        KoinPlatform.getKoin().get<SyncScheduler>().start()
    }
}
