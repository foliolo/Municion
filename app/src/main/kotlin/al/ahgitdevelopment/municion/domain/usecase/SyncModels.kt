package al.ahgitdevelopment.municion.domain.usecase

/**
 * Modelos de datos para el sistema de sincronización
 *
 * @since v3.0.0 (TRACK B Modernization)
 */

/**
 * Excepción personalizada para errores de parseo de Firebase
 * Se reporta a Crashlytics como non-fatal
 */
class FirebaseParseException(message: String) : Exception(message)

/**
 * Información detallada sobre un error de parseo de Firebase
 */
data class ParseError(
    val entity: String,      // "Licencia", "Guia", "Compra", "Tirada"
    val itemKey: String,     // Key del item en Firebase
    val failedField: String, // Campo que falló la validación
    val errorType: String,   // "Missing", "Invalid", "Blank", etc.
    val fieldValue: String?  // Valor del campo (redactado si es PII)
)

/**
 * Resultado de sincronización con información de errores de parseo
 */
data class SyncResultWithErrors(
    val success: Boolean,
    val syncedCount: Int,
    val totalInFirebase: Int,
    val parseErrors: List<ParseError>,
    val hasLocalData: Boolean
) {
    /**
     * Indica si hubo errores de parseo
     */
    val hasParseErrors: Boolean
        get() = parseErrors.isNotEmpty()

    /**
     * Indica si se necesita auto-corrección
     * (Firebase tiene errores Y Room tiene datos válidos)
     */
    val needsAutoFix: Boolean
        get() = hasParseErrors && hasLocalData
}

/**
 * Lista de campos sensibles (PII) que NO deben enviarse a Crashlytics
 */
object SensitiveFields {
    val REDACTED_FIELDS = setOf(
        "numLicencia",
        "numGuia",
        "numArma",
        "nombre",
        "dni",
        "numAbonado",
        "numSeguro"
    )

    /**
     * Verifica si un campo es sensible y debe ser redactado
     */
    fun isSensitive(fieldName: String): Boolean {
        return fieldName in REDACTED_FIELDS
    }

    /**
     * Devuelve el valor redactado o el original según si es PII
     */
    fun redactIfNeeded(fieldName: String, value: String?): String {
        return if (isSensitive(fieldName)) {
            "[REDACTED]"
        } else {
            value?.take(100) ?: "null"
        }
    }
}
