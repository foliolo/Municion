# App Store Screenshots (fastlane snapshot)

Localized App Store screenshots are generated automatically — no manual capture.

## Run

```bash
cd iosApp
bundle exec fastlane ios screenshots          # generates fastlane/screenshots/{es-ES,en-US}/
bundle exec fastlane ios upload_store_assets   # uploads them (+ metadata) to App Store Connect
```

Output: `iosApp/fastlane/screenshots/<lang>/<NN>-<screen>-<device>.png` plus a
`screenshots.html` index to preview them.

## How it works

The app, not the UI test, does the work. Launched with the arguments
`-screenshotMode -screenshotScreen <name>` it:

- skips auth and starts directly on the requested screen,
- skips ads (no UMP/ATT dialogs, no banner, no native ads),
- seeds Room with **fictional** demo data (no real personal data).

The UI test (`iosAppUITests/MunicionScreenshots.swift`) just relaunches the app once per
screen and calls `snapshot(...)` — no UI navigation, so it doesn't break when the layout
changes.

Screens captured: `login`, `licencias`, `guias`, `compras`, `tiradas`, `settings`.

## Key files

| File | Role |
|------|------|
| `shared/.../util/ScreenshotMode.kt` | Flag + start screen, parsed from launch args |
| `shared/.../data/demo/ScreenshotDemoData.kt` | Fictional demo data seeded into Room |
| `shared/.../MainViewController.kt` (iosMain) | Parses the launch args, seeds data |
| `shared/.../ui/main/MainScreen.kt` | Start destination + skips auth nav / banner in screenshot mode |
| `iosApp/iosApp/iOSApp.swift`, `ContentView.swift` | Skip ads/consent in screenshot mode |
| `iosAppUITests/MunicionScreenshots.swift` | The capture test |
| `iosApp/fastlane/Snapfile` | Devices + languages |
| `iosApp/scripts/add_uitest_target.rb` | One-off: created the UI-test target in the Xcode project |

## Devices & languages

Configured in `Snapfile`: **iPhone 16 Pro Max (6.9")** + **iPad Pro 13" (M4)**, in
**es-ES** and **en-US**. App Store Connect only requires one iPhone (6.9"/6.5") and one iPad
(13") size; it scales the rest down automatically.

To add a device/language, edit `Snapfile` — no code changes needed.
