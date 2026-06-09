package al.ahgitdevelopment.municion.platform

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia

/**
 * Creates/removes device-calendar reminders for a licence's expiry: one event on the expiry day and
 * one 30 days before (mirrors the Android `develop` behaviour). Best-effort — callers ignore
 * failures so a missing calendar permission never blocks saving the licence.
 *
 * Android: CalendarContract (needs the WRITE_CALENDAR runtime grant). iOS: EventKit (requests access
 * on demand; needs NSCalendarsUsageDescription in Info.plist).
 */
interface CalendarManager {
    /** iOS requests EventKit access; Android reports whether WRITE_CALENDAR is currently granted. */
    suspend fun ensureAccess(): Boolean

    suspend fun createLicenseExpirationEvents(licencia: Licencia): Result<Unit>

    suspend fun deleteLicenseCalendarEvents(licencia: Licencia): Result<Unit>
}
