package al.ahgitdevelopment.municion.util

/**
 * App Store screenshot mode, activated via the `-screenshotMode` launch argument (fastlane
 * snapshot UI tests). When enabled the app skips auth and ads, seeds fictional demo data and
 * starts directly on [startScreen], so screenshots can be captured unattended on simulators.
 * Never enabled in normal runs.
 */
object ScreenshotMode {
    var enabled: Boolean = false
        private set

    /** One of: login, licencias, guias, compras, tiradas, settings. */
    var startScreen: String = "licencias"
        private set

    fun activate(startScreen: String?) {
        enabled = true
        if (!startScreen.isNullOrBlank()) this.startScreen = startScreen
    }
}
