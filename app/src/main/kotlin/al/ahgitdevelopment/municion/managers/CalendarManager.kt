package al.ahgitdevelopment.municion.managers

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager para eventos de calendario
 *
 * FASE 5: Managers - Extracción desde FragmentMainActivity
 * - Unifica deleteSameDayEventCalendar + deleteMonthBeforeEventCalendar
 * - Proper cursor management (try-finally)
 * - Suspend functions para coroutines
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Singleton
class CalendarManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val crashlytics: FirebaseCrashlytics
) {

    private val contentResolver: ContentResolver
        get() = context.contentResolver

    /**
     * Verifica si tiene permisos de calendario
     */
    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Crea eventos de calendario para una licencia
     * - Evento en la fecha de caducidad
     * - Evento 1 mes antes
     */
    suspend fun createLicenseExpirationEvents(licencia: Licencia): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext Result.failure(SecurityException("No calendar permission"))
        }

        try {
            // Evento el mismo día de caducidad
            createCalendarEvent(
                licencia = licencia,
                daysOffset = 0,
                title = "Tu licencia caduca hoy",
                description = licencia.getDescripcionCalendario()
            )

            // Evento 1 mes antes
            createCalendarEvent(
                licencia = licencia,
                daysOffset = -30,
                title = "Tu licencia caduca dentro de un mes",
                description = licencia.getDescripcionCalendario()
            )

            android.util.Log.i("CalendarManager", "Created calendar events for license: ${licencia.numLicencia}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error creating calendar events", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Elimina eventos de calendario de una licencia
     */
    suspend fun deleteLicenseCalendarEvents(licencia: Licencia): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) {
            return@withContext Result.failure(SecurityException("No calendar permission"))
        }

        try {
            // Eliminar evento mismo día
            deleteCalendarEvent(
                licencia = licencia,
                daysOffset = 0,
                title = "Tu licencia caduca hoy"
            )

            // Eliminar evento 1 mes antes
            deleteCalendarEvent(
                licencia = licencia,
                daysOffset = -30,
                title = "Tu licencia caduca dentro de un mes"
            )

            android.util.Log.i("CalendarManager", "Deleted calendar events for license: ${licencia.numLicencia}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error deleting calendar events", e)
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Crea un evento de calendario
     */
    private fun createCalendarEvent(
        licencia: Licencia,
        daysOffset: Int,
        title: String,
        description: String
    ) {
        val calendar = Calendar.getInstance().apply {
            time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .parse(licencia.fechaCaducidad)!!
            add(Calendar.DAY_OF_MONTH, daysOffset)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
            put(CalendarContract.Events.DTEND, calendar.timeInMillis + (60 * 60 * 1000)) // 1 hora
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.CALENDAR_ID, 1) // ID del calendario principal
            put(CalendarContract.Events.EVENT_TIMEZONE, calendar.timeZone.id)
        }

        contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
    }

    /**
     * Elimina un evento de calendario (UNIFICADO - elimina código duplicado)
     */
    private fun deleteCalendarEvent(
        licencia: Licencia,
        daysOffset: Int,
        title: String
    ) {
        var cursor: Cursor? = null
        try {
            // Calcular fecha inicio y fin del evento
            val startDay: Long
            val endDay: Long

            val beginTime = Calendar.getInstance().apply {
                time = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .parse(licencia.fechaCaducidad)!!
                add(Calendar.DAY_OF_MONTH, daysOffset)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            startDay = beginTime.timeInMillis

            val endTime = Calendar.getInstance().apply {
                time = beginTime.time
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            endDay = endTime.timeInMillis

            // Query para encontrar el evento
            val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART
            )

            val selection = "${CalendarContract.Events.DTSTART} >= ? AND " +
                    "${CalendarContract.Events.DTSTART} <= ? AND " +
                    "${CalendarContract.Events.TITLE} = ? AND " +
                    "${CalendarContract.Events.DESCRIPTION} = ?"

            val selectionArgs = arrayOf(
                startDay.toString(),
                endDay.toString(),
                title,
                licencia.getDescripcionCalendario()
            )

            cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )

            // Eliminar eventos encontrados
            while (cursor?.moveToNext() == true) {
                val eventId = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
                contentResolver.delete(uri, null, null)
                android.util.Log.d("CalendarManager", "Deleted calendar event: $eventId")
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarManager", "Error deleting event", e)
            crashlytics.recordException(e)
        } finally {
            // CRITICAL: Always close cursor
            cursor?.close()
        }
    }
}
