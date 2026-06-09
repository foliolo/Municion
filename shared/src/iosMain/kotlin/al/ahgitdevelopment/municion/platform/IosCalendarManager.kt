@file:OptIn(ExperimentalForeignApi::class, ExperimentalTime::class)

package al.ahgitdevelopment.municion.platform

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.resources.Res
import al.ahgitdevelopment.municion.resources.calendar_event_expires_one_month
import al.ahgitdevelopment.municion.resources.calendar_event_expires_today
import al.ahgitdevelopment.municion.util.parseDdMmYyyy
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.getString
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.coroutines.resume
import kotlin.time.ExperimentalTime

/**
 * iOS [CalendarManager] backed by EventKit (Kotlin/Native Foundation bindings — no Swift bridge).
 * Requests access on demand; requires NSCalendarsUsageDescription in Info.plist (see MIGRATION_REPORT §4.F).
 */
class IosCalendarManager(
    private val crashReporter: CrashReporter,
) : CalendarManager {
    private val store = EKEventStore()

    override suspend fun ensureAccess(): Boolean =
        suspendCancellableCoroutine { cont ->
            store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, _ ->
                cont.resume(granted)
            }
        }

    override suspend fun createLicenseExpirationEvents(licencia: Licencia): Result<Unit> =
        runCatching {
            if (!ensureAccess()) error("Sin acceso al calendario")
            val description = licencia.getDescripcionCalendario()
            insertEvent(licencia, daysOffset = 0, title = getString(Res.string.calendar_event_expires_today), description)
            insertEvent(licencia, daysOffset = -30, title = getString(Res.string.calendar_event_expires_one_month), description)
        }.onFailure { crashReporter.recordException(it) }

    override suspend fun deleteLicenseCalendarEvents(licencia: Licencia): Result<Unit> =
        runCatching {
            if (!ensureAccess()) error("Sin acceso al calendario")
            removeEvents(licencia, daysOffset = 0, title = getString(Res.string.calendar_event_expires_today))
            removeEvents(licencia, daysOffset = -30, title = getString(Res.string.calendar_event_expires_one_month))
        }.onFailure { crashReporter.recordException(it) }

    private fun secondsSince1970(
        ddMmYyyy: String,
        daysOffset: Int,
        hour: Int,
        minute: Int = 0,
        second: Int = 0,
    ): Double? {
        val day = (parseDdMmYyyy(ddMmYyyy) ?: return null).plus(daysOffset, DateTimeUnit.DAY)
        val instant =
            LocalDateTime(day.year, day.month, day.day, hour, minute, second)
                .toInstant(TimeZone.currentSystemDefault())
        return instant.epochSeconds.toDouble()
    }

    private fun insertEvent(
        licencia: Licencia,
        daysOffset: Int,
        title: String,
        description: String,
    ) {
        val startSec = secondsSince1970(licencia.fechaCaducidad, daysOffset, hour = 9) ?: return
        val event = EKEvent.eventWithEventStore(store)
        event.title = title
        event.notes = description
        event.startDate = NSDate.dateWithTimeIntervalSince1970(startSec)
        event.endDate = NSDate.dateWithTimeIntervalSince1970(startSec + ONE_HOUR_SECONDS)
        event.calendar = store.defaultCalendarForNewEvents
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            store.saveEvent(event, EKSpan.EKSpanThisEvent, error.ptr)
        }
    }

    private fun removeEvents(
        licencia: Licencia,
        daysOffset: Int,
        title: String,
    ) {
        val startSec = secondsSince1970(licencia.fechaCaducidad, daysOffset, hour = 0) ?: return
        val endSec = secondsSince1970(licencia.fechaCaducidad, daysOffset, hour = 23, minute = 59, second = 59) ?: return
        val predicate =
            store.predicateForEventsWithStartDate(
                NSDate.dateWithTimeIntervalSince1970(startSec),
                NSDate.dateWithTimeIntervalSince1970(endSec),
                calendars = null,
            )
        store
            .eventsMatchingPredicate(predicate)
            .filterIsInstance<EKEvent>()
            .filter { it.title == title }
            .forEach { event ->
                memScoped {
                    val error = alloc<ObjCObjectVar<NSError?>>()
                    store.removeEvent(event, EKSpan.EKSpanThisEvent, error.ptr)
                }
            }
    }

    private companion object {
        const val ONE_HOUR_SECONDS = 3600.0
    }
}
