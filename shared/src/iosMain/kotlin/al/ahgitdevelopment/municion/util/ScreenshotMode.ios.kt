package al.ahgitdevelopment.municion.util

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

/**
 * App Store archives link the optimized (release) framework, for which
 * [Platform.isDebugBinary] is `false`; fastlane snapshot builds the debug simulator framework
 * (`true`). This is independent of the `appBuildType` Gradle property, which the Xcode embed
 * script does not forward, so it is the reliable signal for gating screenshot mode on iOS.
 */
@OptIn(ExperimentalNativeApi::class)
internal actual fun screenshotModeSupported(): Boolean = Platform.isDebugBinary
