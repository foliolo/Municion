package al.ahgitdevelopment.municion.ui.components

import al.ahgitdevelopment.municion.ui.theme.MunicionTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Compact warning banner used by the four list screens (Licencias, Guias,
 * Compras, Tiradas) when one or more rows have `dataQuality != "ok"`.
 *
 * Renders nothing when [count] is zero, so callers can place it
 * unconditionally at the top of a list. Animates in only when there's
 * something to show.
 *
 * @since v3.3.0 — surfaces the stability-bug aftermath to affected users
 *                 so they know what action to take.
 */
@Composable
fun DataQualityBanner(
    count: Int,
    entityLabel: String,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = bannerText(count, entityLabel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

private fun bannerText(count: Int, entityLabel: String): String {
    val plural = count != 1
    val noun = if (plural) "${entityLabel}s" else entityLabel
    val verb = if (plural) "necesitan" else "necesita"
    return "$count $noun $verb revisión: faltan datos por un bug anterior. " +
        "Ábrelas y recréalas si la información no es correcta."
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataQualityBanner_Single() {
    MunicionTheme {
        DataQualityBanner(count = 1, entityLabel = "guía")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataQualityBanner_Multiple() {
    MunicionTheme {
        DataQualityBanner(count = 3, entityLabel = "licencia")
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDataQualityBanner_Zero() {
    MunicionTheme {
        // Should render nothing
        DataQualityBanner(count = 0, entityLabel = "compra")
    }
}
