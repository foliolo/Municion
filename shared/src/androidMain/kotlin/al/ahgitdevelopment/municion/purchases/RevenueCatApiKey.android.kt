package al.ahgitdevelopment.municion.purchases

import al.ahgitdevelopment.municion.shared.BuildConfig
import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.mp.KoinPlatform

// Play Store key. The sandbox/test key is used ONLY in debuggable builds; every non-debuggable
// (release) APK uses the production key. We key off the app's runtime `debuggable` flag — the same
// signal RevenueCat's SDK uses for its "Wrong API Key" guard — instead of gmazzo's BUILD_TYPE
// (which only reflects -PappBuildType and is absent in local Android Studio release builds, so a
// release APK would otherwise ship the test key and the SDK would close the app on launch).
actual fun revenueCatApiKey(): String {
    val context: Context = KoinPlatform.getKoin().get()
    val isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val prodKey = BuildConfig.REVENUECAT_PLAY_SDK_KEY
    return if (isDebuggable) BuildConfig.REVENUECAT_PLAY_SDK_KEY_TEST.ifBlank { prodKey } else prodKey
}
