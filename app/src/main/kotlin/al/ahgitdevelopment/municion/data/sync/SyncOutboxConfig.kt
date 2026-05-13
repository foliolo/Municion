package al.ahgitdevelopment.municion.data.sync

/**
 * Tunables for the outbox-based write pipeline.
 *
 * @since v3.3.0 (Sync redesign)
 */
object SyncOutboxConfig {

    /** Max number of operations a single worker pass tries to drain. */
    const val BATCH_SIZE = 50

    /** Cap on retries before an op gets marked FAILED and stops being attempted. */
    const val MAX_RETRIES = 10

    /** Backoff base: each retry waits `BASE_DELAY_MS * 2^retryCount`, capped at MAX. */
    const val BACKOFF_BASE_MS = 1_000L
    const val BACKOFF_MAX_MS = 60 * 60 * 1_000L  // 1 hour

    /** Window used by [SyncOperationDao.nextBatch] to skip recently-attempted rows. */
    fun computeBackoffMs(retryCount: Int): Long {
        if (retryCount <= 0) return 0L
        val shift = retryCount.coerceAtMost(20)
        val ms = BACKOFF_BASE_MS shl shift
        return if (ms <= 0 || ms > BACKOFF_MAX_MS) BACKOFF_MAX_MS else ms
    }

    /** "Licencia" | "Guia" | "Compra" | "Tirada" → Firebase collection name. */
    fun firebasePathFor(entityType: String): String = when (entityType) {
        "Licencia" -> "licencias"
        "Guia" -> "guias"
        "Compra" -> "compras"
        "Tirada" -> "tiradas"
        else -> throw IllegalArgumentException("Unknown entity type: $entityType")
    }

    /** Tombstone retention before periodic purge removes the row from Room. */
    const val TOMBSTONE_TTL_MS = 30L * 24 * 60 * 60 * 1_000  // 30 days

    /** Synced outbox rows older than this are purged. */
    const val OUTBOX_SYNCED_TTL_MS = 7L * 24 * 60 * 60 * 1_000  // 7 days
}
