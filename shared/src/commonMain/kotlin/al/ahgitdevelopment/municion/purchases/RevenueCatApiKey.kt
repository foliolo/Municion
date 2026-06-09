package al.ahgitdevelopment.municion.purchases

/**
 * The RevenueCat SDK API key for the current platform/store: Play (Android) vs App Store (iOS),
 * sourced from BuildKonfig (`local.properties`/CI secrets). Blank until keys are provisioned, in
 * which case [RevenueCatRemoveAdsManager] degrades to a no-op.
 */
expect fun revenueCatApiKey(): String
