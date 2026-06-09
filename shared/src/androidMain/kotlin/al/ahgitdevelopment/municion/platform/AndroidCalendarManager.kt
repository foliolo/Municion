package al.ahgitdevelopment.municion.platform

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.calendar_event_expires_one_month
import al.ahgitdevelopment.municion.resources.calendar_event_expires_today
import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Android [CalendarManager] backed by [CalendarContract]. Ported from `develop`'s manager:
 * inserts two events (expiry day + 30 days before) at 09:00 in the primary calendar and removes
 * them by matching title/description on the target day.
 */
class AndroidCalendarManager(
    private val context: Context,
    private val crashReporter: CrashReporter,
) : CalendarManager {
    override suspend fun ensureAccess(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    override suspend fun createLicenseExpirationEvents(licencia: Licencia): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!ensureAccess()) return@withContext Result.failure(SecurityException("Sin permiso de calendario"))
            runCatching {
                val description = licencia.getDescripcionCalendario()
                insertEvent(licencia, daysOffset = 0, title = getString(Res.string.calendar_event_expires_today), description)
                insertEvent(licencia, daysOffset = -30, title = getString(Res.string.calendar_event_expires_one_month), description)
            }.onFailure { crashReporter.recordException(it) }
        }

    override suspend fun deleteLicenseCalendarEvents(licencia: Licencia): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!ensureAccess()) return@withContext Result.failure(SecurityException("Sin permiso de calendario"))
            runCatching {
                val description = licencia.getDescripcionCalendario()
                deleteEvent(licencia, daysOffset = 0, title = getString(Res.string.calendar_event_expires_today), description)
                deleteEvent(licencia, daysOffset = -30, title = getString(Res.string.calendar_event_expires_one_month), description)
            }.onFailure { crashReporter.recordException(it) }
        }

    private fun insertEvent(
        licencia: Licencia,
        daysOffset: Int,
        title: String,
        description: String,
    ) {
        val calendar = dayAt(licencia.fechaCaducidad, daysOffset, hour = 9)
        val values =
            ContentValues().apply {
                put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                put(CalendarContract.Events.DTEND, calendar.timeInMillis + ONE_HOUR_MILLIS)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, PRIMARY_CALENDAR_ID)
                put(CalendarContract.Events.EVENT_TIMEZONE, calendar.timeZone.id)
            }
        context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    private fun deleteEvent(
        licencia: Licencia,
        daysOffset: Int,
        title: String,
        description: String,
    ) {
        val start = dayAt(licencia.fechaCaducidad, daysOffset, hour = 0).timeInMillis
        val end = dayAt(licencia.fechaCaducidad, daysOffset, hour = 23, minute = 59, second = 59).timeInMillis
        val projection = arrayOf(CalendarContract.Events._ID)
        val selection =
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ? AND " +
                "${CalendarContract.Events.TITLE} = ? AND ${CalendarContract.Events.DESCRIPTION} = ?"
        val args = arrayOf(start.toString(), end.toString(), title, description)
        context.contentResolver
            .query(CalendarContract.Events.CONTENT_URI, projection, selection, args, null)
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, cursor.getLong(0))
                    context.contentResolver.delete(uri, null, null)
                }
            }
    }

    private fun dayAt(
        ddMmYyyy: String,
        daysOffset: Int,
        hour: Int,
        minute: Int = 0,
        second: Int = 0,
    ): Calendar =
        Calendar.getInstance().apply {
            time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(ddMmYyyy)!!
            add(Calendar.DAY_OF_MONTH, daysOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
        }

    private companion object {
        const val ONE_HOUR_MILLIS = 60L * 60L * 1000L
        const val PRIMARY_CALENDAR_ID = 1L
    }
}
