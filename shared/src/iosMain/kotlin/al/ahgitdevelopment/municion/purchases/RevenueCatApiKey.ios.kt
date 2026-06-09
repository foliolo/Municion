package al.ahgitdevelopment.municion.purchases

import al.ahgitdevelopment.municion.BuildConfig

// App Store key (RevenueCat configured for the App Store project).
actual fun revenueCatApiKey(): String = BuildConfig.REVENUECAT_APPSTORE_SDK_KEY
