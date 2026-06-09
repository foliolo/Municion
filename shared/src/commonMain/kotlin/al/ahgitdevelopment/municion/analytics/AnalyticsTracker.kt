package al.ahgitdevelopment.municion.analytics

/**
 * Minimal analytics abstraction. Privacy: pass metadata only — never free-text user content
 * (license numbers, names, DNI…).
 */
interface AnalyticsTracker {
    fun setEnabled(enabled: Boolean)
    fun setUserId(userId: String?)

    /** Param values must be String/Int/Long/Double/Boolean; others are dropped. */
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())

    fun logScreenView(screenName: String, screenClass: String? = null)
}
