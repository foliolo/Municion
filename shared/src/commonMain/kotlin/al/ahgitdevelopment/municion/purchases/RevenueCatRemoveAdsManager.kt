package al.ahgitdevelopment.municion.purchases

import al.ahgitdevelopment.municion.ads.RemoveAdsManager
import al.ahgitdevelopment.municion.firebase.CrashReporter
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RevenueCat-backed [RemoveAdsManager] using the official multiplatform `purchases-kmp` SDK
 * (common API — no expect/actual, no Swift bridge; the iOS native dependency is linked by Gradle).
 *
 * RevenueCat is the source of truth for the `ad_free` entitlement; the SDK persists it locally so
 * ad-gating keeps working offline. If [apiKey] is blank (keys not configured yet) this degrades to
 * a no-op (ads always shown), so it is safe as the default binding before keys are provisioned.
 */
class RevenueCatRemoveAdsManager(
    private val apiKey: String,
    private val settings: Settings,
    private val crashReporter: CrashReporter,
) : RemoveAdsManager {
    // Seeded from the local cache so a premium user starts ad-free at cold start (no ad flash while
    // RevenueCat refreshes over the network); the next refresh corrects the cache if it's stale.
    private val _hasRemovedAds = MutableStateFlow(settings.getBoolean(KEY_AD_FREE_CACHE, false))
    override val hasRemovedAds: StateFlow<Boolean> = _hasRemovedAds.asStateFlow()

    private fun setAdFree(active: Boolean) {
        _hasRemovedAds.value = active
        settings.putBoolean(KEY_AD_FREE_CACHE, active)
    }

    override suspend fun initialize(userId: String?) {
        if (apiKey.isBlank()) return // Keys not configured: behave as a no-op.
        try {
            if (!Purchases.isConfigured) {
                Purchases.logLevel = LogLevel.WARN
                Purchases.configure(
                    PurchasesConfiguration(apiKey) {
                        if (!userId.isNullOrBlank()) appUserId = userId
                    },
                )
            } else if (!userId.isNullOrBlank() && Purchases.sharedInstance.appUserID != userId) {
                Purchases.sharedInstance.awaitLogIn(userId)
            }
            refreshEntitlement()
        } catch (e: Throwable) {
            crashReporter.recordException(e)
        }
    }

    override suspend fun purchaseRemoveAds(): Result<Boolean> =
        runCatching {
            check(Purchases.isConfigured) { "RevenueCat no está configurado" }
            val offering =
                Purchases.sharedInstance.awaitOfferings().current
                    ?: error("RevenueCat no tiene un offering por defecto")
            // Remove-ads is a one-time lifetime non-consumable: pick the lifetime package explicitly,
            // never availablePackages.first() (the offering may also expose sample subscription packages
            // such as Monthly, which would charge the wrong product).
            val pkg =
                offering.lifetime
                    ?: error("El offering de RevenueCat no tiene un paquete lifetime")
            val active =
                Purchases.sharedInstance
                    .awaitPurchase(pkg)
                    .customerInfo
                    .isAdFree()
            setAdFree(active)
            active
        }.onFailure { crashReporter.recordException(it) }

    override suspend fun restore(): Result<Boolean> =
        runCatching {
            check(Purchases.isConfigured) { "RevenueCat no está configurado" }
            val active = Purchases.sharedInstance.awaitRestore().isAdFree()
            setAdFree(active)
            active
        }.onFailure { crashReporter.recordException(it) }

    private suspend fun refreshEntitlement() {
        setAdFree(Purchases.sharedInstance.awaitCustomerInfo().isAdFree())
    }

    private fun CustomerInfo.isAdFree(): Boolean = entitlements[AD_FREE_ENTITLEMENT]?.isActive == true

    private companion object {
        const val AD_FREE_ENTITLEMENT = "ad_free"
        const val KEY_AD_FREE_CACHE = "ad_free_entitlement_cache"
    }
}
