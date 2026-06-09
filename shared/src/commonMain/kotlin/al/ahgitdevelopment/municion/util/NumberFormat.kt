package al.ahgitdevelopment.municion.util

import kotlin.math.abs
import kotlin.math.roundToLong

/** Multiplatform replacement for `String.format("%.2f", value)`. */
fun formatTwoDecimals(value: Double): String {
    val cents = (value * 100).roundToLong()
    val negative = cents < 0
    val absCents = abs(cents)
    val intPart = absCents / 100
    val frac = (absCents % 100).toString().padStart(2, '0')
    return "${if (negative) "-" else ""}$intPart.$frac"
}

/** Price formatted with two decimals and a trailing euro sign, e.g. "25.00€". */
fun formatPriceEuro(value: Double): String = "${formatTwoDecimals(value)}€"
