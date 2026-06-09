package al.ahgitdevelopment.municion.domain.usecase

/** Non-fatal exception reported when a Firebase record can't be parsed. */
class FirebaseParseException(
    message: String,
) : Exception(message)

/** Detail about a Firebase parse failure (PII redacted). */
data class ParseError(
    val entity: String,
    val itemKey: String,
    val failedField: String,
    val errorType: String,
    val fieldValue: String?,
)

/** Per-collection sync result. */
data class SyncResultWithErrors(
    val success: Boolean,
    val syncedCount: Int,
    val totalInFirebase: Int,
    val parseErrors: List<ParseError>,
    val hasLocalData: Boolean,
) {
    val hasParseErrors: Boolean get() = parseErrors.isNotEmpty()
    val needsAutoFix: Boolean get() = hasParseErrors && hasLocalData
}

/** PII fields never sent to Crashlytics. */
object SensitiveFields {
    val REDACTED_FIELDS =
        setOf(
            "numLicencia",
            "numGuia",
            "numArma",
            "nombre",
            "dni",
            "numAbonado",
            "numSeguro",
        )

    fun isSensitive(fieldName: String): Boolean = fieldName in REDACTED_FIELDS

    fun redactIfNeeded(
        fieldName: String,
        value: String?,
    ): String = if (isSensitive(fieldName)) "[REDACTED]" else value?.take(100) ?: "null"
}
