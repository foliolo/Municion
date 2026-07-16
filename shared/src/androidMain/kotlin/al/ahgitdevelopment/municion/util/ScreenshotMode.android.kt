package al.ahgitdevelopment.municion.util

import al.ahgitdevelopment.municion.shared.BuildConfig

/**
 * Release APKs/AABs are built with `-PappBuildType=release` (see androidApp fastlane), so screenshot
 * mode is available only in debug/QA builds and is fully inert in production.
 */
internal actual fun screenshotModeSupported(): Boolean = BuildConfig.BUILD_TYPE != "release"
