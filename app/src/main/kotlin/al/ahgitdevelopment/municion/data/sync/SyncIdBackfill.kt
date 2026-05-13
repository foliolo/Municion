package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Idempotent safety-net pass that ensures every entity row has a real
 * deterministic syncId.
 *
 * Since v3.3.0 the heavy lifting is done by
 * [al.ahgitdevelopment.municion.data.local.room.MunicionDatabase.Companion.MIGRATION_32_33],
 * which assigns deterministic UUIDs to every row as part of the migration
 * transaction. This class runs at startup as a defense in depth:
 *
 *  - Re-runs the deterministic UUID assignment for any row whose `sync_id`
 *    is empty or matches the legacy placeholder pattern
 *    `00000000-0000-0000-0000-...`. This catches the unlikely case where
 *    the migration was interrupted before completing.
 *
 *  - Repairs `compras.guia_sync_id` for any row whose link is missing but
 *    whose `id_pos_guia` still points to an existing guia.
 *
 * Calling [run] is safe in any state:
 *
 *  - On a freshly migrated database, it finds zero placeholder rows and
 *    is a near-instant no-op.
 *
 *  - On a partially-migrated database, it finishes the job.
 *
 *  - On a fully-migrated, already-backfilled database, it's a no-op.
 *
 * @since v3.3.0 (Sync redesign)
 */
@Singleton
class SyncIdBackfill @Inject constructor(
    private val database: MunicionDatabase
) {

    companion object {
        private const val TAG = "SyncIdBackfill"
        private const val PLACEHOLDER_PREFIX = "00000000-0000-0000-0000-"

        private val TABLES: List<Pair<String, String>> = listOf(
            "licencias" to "Licencia",
            "guias" to "Guia",
            "compras" to "Compra",
            "tiradas" to "Tirada"
        )
    }

    suspend fun run() {
        var totalBackfilled = 0
        for ((table, entityType) in TABLES) {
            totalBackfilled += backfillTable(table, entityType)
        }
        totalBackfilled += repairCompraGuiaSyncId()

        if (totalBackfilled > 0) {
            Log.i(TAG, "Backfilled $totalBackfilled sync_id values (safety net)")
        } else {
            Log.d(TAG, "No sync_id placeholders found; backfill is a no-op")
        }
    }

    private fun backfillTable(table: String, entityType: String): Int {
        val db = database.openHelper.writableDatabase
        val toFix = mutableListOf<Int>()
        db.query(
            SimpleSQLiteQuery(
                "SELECT id FROM $table WHERE sync_id = '' OR sync_id LIKE ?",
                arrayOf<Any?>("$PLACEHOLDER_PREFIX%")
            )
        ).use { cursor ->
            while (cursor.moveToNext()) {
                toFix += cursor.getInt(0)
            }
        }

        if (toFix.isEmpty()) return 0

        var count = 0
        db.beginTransaction()
        try {
            for (legacyId in toFix) {
                val newSyncId = SyncIdGenerator.deterministicSyncId(entityType, legacyId)
                db.execSQL(
                    "UPDATE $table SET sync_id = ? WHERE id = ?",
                    arrayOf<Any?>(newSyncId, legacyId)
                )
                count++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        Log.i(TAG, "Backfilled $count sync_id values in $table")
        return count
    }

    private fun repairCompraGuiaSyncId(): Int {
        val db = database.openHelper.writableDatabase
        db.execSQL(
            """
            UPDATE compras
            SET guia_sync_id = (
                SELECT g.sync_id FROM guias g WHERE g.id = compras.id_pos_guia LIMIT 1
            )
            WHERE guia_sync_id IS NULL
            """.trimIndent()
        )

        var orphans = 0
        db.query(SimpleSQLiteQuery("SELECT COUNT(*) FROM compras WHERE guia_sync_id IS NULL")).use {
            if (it.moveToNext()) orphans = it.getInt(0)
        }
        if (orphans > 0) {
            Log.w(TAG, "Found $orphans orphan compras (parent Guia missing)")
        }
        return 0  // we don't count linked rows here; the migration handled the bulk
    }
}
