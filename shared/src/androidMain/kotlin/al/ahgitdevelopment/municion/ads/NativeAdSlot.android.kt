package al.ahgitdevelopment.municion.ads

import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.ad_attribution_label
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import org.jetbrains.compose.resources.stringResource

/**
 * Renders a native advanced ad as a Material-styled card: a full-width media area on top (≥120dp so
 * AdMob doesn't demonetize the slot) with the "Publicidad" badge + headline + body + CTA below. Asset
 * views are real Android Views registered on the [NativeAdView] (required by the SDK); colors come
 * from the current [MaterialTheme] so the card blends with the entity list. The SDK overlays the
 * mandatory AdChoices icon automatically.
 */
@Composable
actual fun NativeAdSlot(
    adHandle: NativeAdHandle,
    modifier: Modifier,
) {
    val nativeAd = adHandle as? NativeAd ?: return
    val attribution = stringResource(Res.string.ad_attribution_label)
    val colors =
        NativeAdColors(
            surface = MaterialTheme.colorScheme.surfaceVariant.toArgb(),
            onSurface = MaterialTheme.colorScheme.onSurface.toArgb(),
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
            primary = MaterialTheme.colorScheme.primary.toArgb(),
            onPrimary = MaterialTheme.colorScheme.onPrimary.toArgb(),
            badgeBackground = MaterialTheme.colorScheme.tertiaryContainer.toArgb(),
            onBadge = MaterialTheme.colorScheme.onTertiaryContainer.toArgb(),
        )

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context -> buildNativeAdView(context, colors) },
        update = { adView -> bindNativeAd(adView, nativeAd, attribution) },
    )
}

private data class NativeAdColors(
    val surface: Int,
    val onSurface: Int,
    val onSurfaceVariant: Int,
    val primary: Int,
    val onPrimary: Int,
    val badgeBackground: Int,
    val onBadge: Int,
)

private class NativeAdViews(
    val mediaContainer: View,
    val media: MediaView,
    val badge: TextView,
    val headline: TextView,
    val body: TextView,
    val cta: TextView,
)

// AdMob demonetizes native media views whose width or height is < 120dp; keep the media well above it.
private const val MEDIA_HEIGHT_DP = 140

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

private fun rounded(
    color: Int,
    radiusPx: Float,
): GradientDrawable =
    GradientDrawable().apply {
        cornerRadius = radiusPx
        setColor(color)
    }

private fun buildNativeAdView(
    context: Context,
    colors: NativeAdColors,
): NativeAdView {
    val pad = context.dp(12)

    val card =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(colors.surface, context.dp(12).toFloat())
            clipToOutline = true
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

    val mediaContainer =
        FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.dp(MEDIA_HEIGHT_DP))
        }
    val media =
        MediaView(context).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        }
    mediaContainer.addView(media)

    val contentRow =
        LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(pad, pad, pad, pad)
            layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

    val textColumn =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams =
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = context.dp(12) }
        }
    val badge =
        TextView(context).apply {
            background = rounded(colors.badgeBackground, context.dp(4).toFloat())
            setTextColor(colors.onBadge)
            textSize = 10f
            setPadding(context.dp(6), context.dp(2), context.dp(6), context.dp(2))
            layoutParams =
                LinearLayout
                    .LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ).apply { bottomMargin = context.dp(4) }
        }
    val headline =
        TextView(context).apply {
            setTextColor(colors.onSurface)
            textSize = 16f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
    val body =
        TextView(context).apply {
            setTextColor(colors.onSurfaceVariant)
            textSize = 13f
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
        }
    textColumn.addView(badge)
    textColumn.addView(headline)
    textColumn.addView(body)

    val cta =
        TextView(context).apply {
            background = rounded(colors.primary, context.dp(20).toFloat())
            setTextColor(colors.onPrimary)
            textSize = 13f
            gravity = Gravity.CENTER
            isAllCaps = false
            setPadding(context.dp(16), context.dp(8), context.dp(16), context.dp(8))
            layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

    contentRow.addView(textColumn)
    contentRow.addView(cta)

    card.addView(mediaContainer)
    card.addView(contentRow)

    return NativeAdView(context).apply {
        layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addView(card)
        // Register asset views so AdMob handles clicks/impressions and AdChoices placement.
        mediaView = media
        headlineView = headline
        bodyView = body
        callToActionView = cta
        tag = NativeAdViews(mediaContainer, media, badge, headline, body, cta)
    }
}

private fun bindNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    attribution: String,
) {
    val views = adView.tag as? NativeAdViews ?: return

    views.badge.text = attribution
    views.headline.text = nativeAd.headline.orEmpty()

    val bodyText = nativeAd.body
    if (bodyText.isNullOrBlank()) {
        views.body.visibility = View.GONE
    } else {
        views.body.visibility = View.VISIBLE
        views.body.text = bodyText
    }

    val ctaText = nativeAd.callToAction
    if (ctaText.isNullOrBlank()) {
        views.cta.visibility = View.GONE
    } else {
        views.cta.visibility = View.VISIBLE
        views.cta.text = ctaText
    }

    val mediaContent = nativeAd.mediaContent
    if (mediaContent != null) {
        views.media.mediaContent = mediaContent
        views.mediaContainer.visibility = View.VISIBLE
    } else {
        views.mediaContainer.visibility = View.GONE
    }

    adView.setNativeAd(nativeAd)
}
