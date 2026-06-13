package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.data.demo.seedScreenshotDemoData
import al.ahgitdevelopment.municion.di.initKoin
import al.ahgitdevelopment.municion.util.ScreenshotMode
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    // Fastlane snapshot passes -screenshotMode/-screenshotScreen to capture App Store screenshots.
    val args = NSProcessInfo.processInfo.arguments.map { it.toString() }
    if (args.contains("-screenshotMode")) {
        val idx = args.indexOf("-screenshotScreen")
        ScreenshotMode.activate(if (idx >= 0) args.getOrNull(idx + 1) else null)
    }

    return ComposeUIViewController(
        configure = {
            initKoin()
            if (ScreenshotMode.enabled) {
                MainScope().launch { seedScreenshotDemoData(KoinPlatform.getKoin().get()) }
            }
        },
    ) {
        App()
    }
}
