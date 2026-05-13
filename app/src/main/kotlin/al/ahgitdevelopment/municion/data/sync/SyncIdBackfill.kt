package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Replaces the placeholder sync_id values inserted by
 * [al.ahgitdevelopment.municion.data.local.room.MunicionDatabase.Companion.MIGRATION_32_33]
 * with proper deterministic UUIDs derived from `table:legacyId`.
 *
 * Idempotent: skips rows whose sync_id is already a real UUID.
 *
 * Also backfills `compras.guia_sync_id` from the parent guia's sync_id
 * (using `compras.id_pos_guia` as the join key, since v3.x stored that
 * as a positional reference but in practice it has held the parent
 * guia.id since v3.0.0+).
 *
 * Called from [al.ahgitdevelopment.municion.MunicionApplication.onCreate]
 * once on each app start. The op is cheap and idempotent after the first
 * full pass; subsequent runs only touch new rows (there shouldn't be any
 * because we now generate the syncId in code on insert).
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

        /** Maps Room table names to the entity-type label used by [SyncIdGenerator]. */
        private val TABLES: List<Pair<String, String>> = listOf(
            "licencias" to "Licencia",
            "guias" to "Guia",
            "compras" to "Compra",
            "tiradas" to "Tirada"
        )
    }

    suspend fun run() {
        val db = database.openHelper.writableDatabase
        var totalBackfilled = 0

        for ((table, entityType) in TABLES) {
            totalBackfilled += backfillTable(table, entityType)
        }

        totalBackfilled += backfillCompraGuiaSyncId()

        if (totalBackfilled > 0) {
            Log.i(TAG, "Backfilled $totalBackfilled sync_id values")
        } else {
            Log.d(TAG, "No sync_id placeholders found; backfill is a no-op")
        }
    }

    private fun backfillTable(table: String, entityType: String): Int {
        val db = database.openHelper.writableDatabase
        // Read the (id, sync_id) of every row that still has a placeholder.
        val placeholders = mutableListOf<Pair<Int, String>>()
        db.query(
            SimpleSQLiteQuery(
                "SELECT id, sync_id FROM $table WHERE sync_id LIKE ?",
                arrayOf<Any?>("$PLACEHOLDER_PREFIX%")
            )
        ).use { cursor ->
            while (cursor.moveToNext()) {
                placeholders += cursor.getInt(0) to cursor.getString(1)
            }
        }

        if (placeholders.isEmpty()) return 0

        var count = 0
        db.beginTransaction()
        try {
            for ((legacyId, _) in placeholders) {
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

    /**
     * Populates compras.guia_sync_id by joining on the existing positional
     * reference compras.id_pos_guia → guias.id. Rows where the parent
     * Guia no longer exists (orphan compras from the catastrophic deletion
     * chain) get an empty string sentinel so the sync layer can flag them
     * as `dataQuality='degraded'` rather than silently lose them.
     */
    private fun backfillCompraGuiaSyncId(): Int {
        val db = database.openHelper.writableDatabase

        db.execSQL(
            """
            UPDATE compras
            SET guia_sync_id = (
                SELECT g.sync_id FROM guias g
                WHERE g.id = compras.id_pos_guia
                LIMIT 1
            )
            WHERE guia_sync_id IS NULL
            """.trimIndent()
        )

        // Count how many were filled vs orphan
        var filled = 0
        db.query(SimpleSQLiteQuery("SELECT COUNT(*) FROM compras WHERE guia_sync_id IS NOT NULL")).use {
            if (it.moveToNext()) filled = it.getInt(0)
        }
        var orphans = 0
        db.query(SimpleSQLiteQuery("SELECT COUNT(*) FROM compras WHERE guia_sync_id IS NULL")).use {
            if (it.moveToNext()) orphans = it.getInt(0)
        }

        if (orphans > 0) {
            Log.w(TAG, "Found $orphans orphan compras (parent Guia missing)")
        }
        Log.i(TAG, "Linked $filled compras to their parent Guia via guia_sync_id")
        return filled
    }
}
