package al.ahgitdevelopment.municion.util

/**
 * App Store screenshot mode, activated via the `-screenshotMode` launch argument (fastlane
 * snapshot UI tests). When enabled the app skips auth and ads, seeds fictional demo data and
 * starts directly on [startScreen], so screenshots can be captured unattended on simulators.
 *
 * This is QA-only tooling: it is available exclusively in debug/simulator builds
 * ([screenshotModeSupported]). In release / App Store builds [activate] is a no-op, so the
 * auth-skip and demo-seeding paths can never run in production and there is no reachable
 * hidden/undocumented behaviour in the shipped binary.
 */
object ScreenshotMode {
    var enabled: Boolean = false
        private set

    /** One of: login, licencias, guias, compras, tiradas, settings. */
    var startScreen: String = "licencias"
        private set

    fun activate(startScreen: String?) {
        // Guard: never enable in release/App Store builds, whatever launch arguments are present.
        if (!screenshotModeSupported()) return
        enabled = true
        if (!startScreen.isNullOrBlank()) this.startScreen = startScreen
    }
}

/**
 * True only for debug/QA builds (the fastlane simulator builds that capture screenshots). Release /
 * App Store builds return false so [ScreenshotMode] stays permanently inert.
 */
internal expect fun screenshotModeSupported(): Boolean
