package al.ahgitdevelopment.municion.purchases

import al.ahgitdevelopment.municion.shared.BuildConfig

// App Store key (RevenueCat configured for the App Store project).
actual fun revenueCatApiKey(): String = BuildConfig.REVENUECAT_APPSTORE_SDK_KEY
