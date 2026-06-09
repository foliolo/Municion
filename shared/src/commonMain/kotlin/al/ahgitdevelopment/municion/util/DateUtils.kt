@file:OptIn(ExperimentalTime::class)

package al.ahgitdevelopment.municion.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Date helpers for the "dd/MM/yyyy" string format used across the data model.
 * Multiplatform replacement for the Android `SimpleDateFormat`/`Calendar` usage.
 */

private val SPANISH_MONTHS = arrayOf(
    "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic",
)

/** Parses a "dd/MM/yyyy" string into a [LocalDate], or null if malformed/invalid. */
fun parseDdMmYyyy(value: String): LocalDate? {
    val parts = value.split("/")
    if (parts.size != 3) return null
    val day = parts[0].trim().toIntOrNull() ?: return null
    val month = parts[1].trim().toIntOrNull() ?: return null
    val year = parts[2].trim().toIntOrNull() ?: return null
    return try {
        LocalDate(year, month, day)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/** Today in the system time zone. */
fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

/** Whole days from today to [target] (negative when [target] is in the past). */
fun daysFromTodayTo(target: LocalDate): Long =
    target.toEpochDays() - today().toEpochDays()

/** Whole days elapsed since [target] (negative when [target] is in the future). */
fun daysSince(target: LocalDate): Long =
    today().toEpochDays() - target.toEpochDays()

/** Formats a "dd/MM/yyyy" string as "dd MMM yyyy" (Spanish month abbreviations); falls back to the input. */
fun formatDisplayDate(ddMmYyyy: String): String {
    val parts = ddMmYyyy.split("/")
    if (parts.size != 3) return ddMmYyyy
    val day = parts[0].trim().toIntOrNull() ?: return ddMmYyyy
    val month = parts[1].trim().toIntOrNull() ?: return ddMmYyyy
    val year = parts[2].trim()
    if (month !in 1..12) return ddMmYyyy
    return "${day.toString().padStart(2, '0')} ${SPANISH_MONTHS[month - 1]} $year"
}
