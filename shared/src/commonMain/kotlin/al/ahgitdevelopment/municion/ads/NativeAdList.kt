package al.ahgitdevelopment.municion.ads

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Emits [items] into a `LazyColumn`, inserting a [NativeAdSlot] after every [adFrequency] items.
 * Ads cycle through the [nativeAds] pool with modulo, so a small pool covers an arbitrarily long
 * list. An empty pool (ads not loaded yet, or removed via purchase) renders the plain list — no
 * extra branching at the call site, and previews stay ad-free.
 *
 * Ad slots use index-based string keys (`native_ad_<index>`) which never collide with the entity
 * keys produced by [key].
 */
fun <T> LazyListScope.itemsWithNativeAds(
    items: List<T>,
    nativeAds: List<NativeAdHandle>,
    key: (T) -> Any,
    adFrequency: Int = NativeAdManager.AD_FREQUENCY,
    itemContent: @Composable (T) -> Unit,
) {
    items.forEachIndexed { index, item ->
        item(key = key(item)) { itemContent(item) }

        val isAdBoundary = (index + 1) % adFrequency == 0
        if (isAdBoundary && nativeAds.isNotEmpty()) {
            val adIndex = (index + 1) / adFrequency - 1
            val adHandle = nativeAds[adIndex % nativeAds.size]
            item(key = "native_ad_$index") {
                NativeAdSlot(adHandle = adHandle, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
