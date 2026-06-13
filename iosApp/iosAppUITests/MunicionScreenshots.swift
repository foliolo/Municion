import XCTest

/// Captures App Store screenshots via fastlane snapshot.
///
/// The app does all the heavy lifting: launched with `-screenshotMode -screenshotScreen <name>`
/// it skips auth and ads, seeds fictional demo data into Room and starts directly on the
/// requested screen (see ScreenshotMode.kt / ScreenshotDemoData.kt in the shared module). This
/// test only relaunches the app per screen and shoots — no UI navigation, no flakiness.
// @MainActor: SnapshotHelper's setupSnapshot/snapshot are main-actor-isolated; UI tests run on the
// main thread anyway, so isolating the whole class keeps Swift 6 strict concurrency happy.
@MainActor
final class MunicionScreenshots: XCTestCase {
    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testCaptureScreenshots() {
        let screens = ["login", "licencias", "guias", "compras", "tiradas", "settings"]
        for (index, screen) in screens.enumerated() {
            let app = XCUIApplication()
            setupSnapshot(app)
            app.launchArguments += ["-screenshotMode", "-screenshotScreen", screen]
            app.launch()
            // Give Compose + Room demo seed time to settle before shooting.
            sleep(6)
            snapshot(String(format: "%02d-%@", index + 1, screen))
            app.terminate()
        }
    }
}
