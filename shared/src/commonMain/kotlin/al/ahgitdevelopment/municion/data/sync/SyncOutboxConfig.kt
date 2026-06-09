package al.ahgitdevelopment.municion.data.sync

/** Tunables for the outbox-based write pipeline. */
object SyncOutboxConfig {
    /** Max number of operations a single drain pass tries. */
    const val BATCH_SIZE = 50

    /** Cap on retries before an op is marked FAILED. */
    const val MAX_RETRIES = 10

    const val BACKOFF_BASE_MS = 1_000L
    const val BACKOFF_MAX_MS = 60 * 60 * 1_000L // 1 hour

    /** Exponential backoff: retry 1 → BASE, retry 2 → 2·BASE, … capped at [BACKOFF_MAX_MS]. */
    fun computeBackoffMs(retryCount: Int): Long {
        if (retryCount <= 0) return 0L
        val shift = (retryCount - 1).coerceAtMost(20)
        val ms = BACKOFF_BASE_MS shl shift
        return if (ms <= 0 || ms > BACKOFF_MAX_MS) BACKOFF_MAX_MS else ms
    }

    /** Entity type → Firebase collection name. */
    fun firebasePathFor(entityType: String): String =
        when (entityType) {
            "Licencia" -> "licencias"
            "Guia" -> "guias"
            "Compra" -> "compras"
            "Tirada" -> "tiradas"
            else -> throw IllegalArgumentException("Unknown entity type: $entityType")
        }

    /** Tombstone retention before periodic purge removes the row from Room. */
    const val TOMBSTONE_TTL_MS = 30L * 24 * 60 * 60 * 1_000 // 30 days

    /** Synced outbox rows older than this are purged. */
    const val OUTBOX_SYNCED_TTL_MS = 7L * 24 * 60 * 60 * 1_000 // 7 days
}
