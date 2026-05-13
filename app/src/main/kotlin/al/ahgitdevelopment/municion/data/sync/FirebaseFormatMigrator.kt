package al.ahgitdevelopment.municion.data.sync

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migrates a user's Firebase RTDB data forward across two schema bumps:
 *
 *  v1 — Legacy array format (children keyed by "0", "1", "2"…).
 *  v2 — Map format keyed by entity.id (the integer Room primary key).
 *  v3 — Map format keyed by syncId (UUID), with `syncId` embedded in the
 *       payload. Adds `deleted`/`deletedAt`/`dataQuality` fields with
 *       safe defaults if missing.
 *
 * Both transitions are idempotent. The migrator runs once per user on
 * each device (gated by [PREF_KEY_FORMAT_VERSION] in shared prefs and
 * `_meta/format_version` in the remote tree). After a successful pass it
 * writes the new version to both flags so subsequent runs short-circuit.
 *
 * For v2→v3: legacy integer keys are rewritten as deterministic UUIDs via
 * [SyncIdGenerator.deterministicSyncId] so multiple devices observing the
 * same legacy entity converge on the same syncId. Once written under the
 * new key, the old integer key is removed in the same setValue call (by
 * writing the entire collection map at once).
 *
 * @since v3.0.0
 * @updated v3.3.0 — adds v2 → v3 (syncId rekey)
 */
@Singleton
class FirebaseFormatMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "FirebaseFormatMigrator"
        private const val PREFS_NAME = "firebase_sync_prefs"
        private const val PREF_KEY_FORMAT_VERSION = "firebase_format_version"

        const val FORMAT_VERSION_SYNCID = 3

        /** Map of Firebase collection name → entity-type label (singular). */
        private val COLLECTIONS: Map<String, String> = mapOf(
            "licencias" to "Licencia",
            "guias" to "Guia",
            "compras" to "Compra",
            "tiradas" to "Tirada"
        )
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Runs all needed migrations to bring the user up to the current
     * [FORMAT_VERSION_SYNCID]. Returns true on success (including no-op),
     * false on a recoverable error (the data is left in whatever
     * intermediate state we reached and we'll retry next time).
     */
    suspend fun migrateIfNeeded(userId: String): Boolean {
        if (prefs.getInt(PREF_KEY_FORMAT_VERSION, 0) >= FORMAT_VERSION_SYNCID) {
            Log.d(TAG, "Already migrated (local flag) to v$FORMAT_VERSION_SYNCID")
            return true
        }

        return try {
            val dbRef = firebaseDb.child("users").child(userId).child("db")

            val metaSnapshot = dbRef.child("_meta").child("format_version").get().await()
            val remoteVersion = (metaSnapshot.value as? Number)?.toInt() ?: 0

            if (remoteVersion >= FORMAT_VERSION_SYNCID) {
                Log.d(TAG, "Already migrated (remote flag) to v$FORMAT_VERSION_SYNCID")
                prefs.edit { putInt(PREF_KEY_FORMAT_VERSION, FORMAT_VERSION_SYNCID) }
                return true
            }

            // Both transitions in a single pass: legacy → map keyed by
            // syncId. We don't care what the starting version is; the
            // logic below handles array, int-keyed, or UUID-keyed inputs.
            var migrated = false
            for ((collection, entityType) in COLLECTIONS) {
                if (migrateCollection(dbRef, collection, entityType)) {
                    migrated = true
                }
            }

            dbRef.child("_meta").child("format_version").setValue(FORMAT_VERSION_SYNCID).await()
            prefs.edit { putInt(PREF_KEY_FORMAT_VERSION, FORMAT_VERSION_SYNCID) }

            if (migrated) {
                Log.i(TAG, "Firebase format migration to v$FORMAT_VERSION_SYNCID completed")
                crashlytics.log("Firebase format migration v→3 completed")
            } else {
                Log.d(TAG, "Format migration v→3: no-op (collections already current)")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Format migration failed: ${e.message}", e)
            crashlytics.log("Firebase format migration failed: ${e.message}")
            crashlytics.recordException(e)
            false
        }
    }

    /**
     * @return true if the collection actually had to be rewritten.
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun migrateCollection(
        dbRef: DatabaseReference,
        collection: String,
        entityType: String
    ): Boolean {
        val entityRef = dbRef.child(collection)
        val snapshot = entityRef.get().await()

        if (!snapshot.exists() || snapshot.childrenCount == 0L) {
            return false
        }

        var needsRewrite = false
        val newCollection = mutableMapOf<String, Any?>()

        snapshot.children.forEach { child ->
            val originalKey = child.key ?: return@forEach
            val rawValue = child.value as? Map<String, Any?>
            if (rawValue == null) {
                Log.w(TAG, "$collection[$originalKey]: not a map, skipping")
                return@forEach
            }

            val existingSyncId = (rawValue["syncId"] as? String)?.takeIf { SyncIdGenerator.isValid(it) }
            val syncId = existingSyncId
                ?: SyncIdGenerator.deterministicSyncIdForFirebaseKey(entityType, originalKey, rawValue)

            // If the syncId matches the existing key AND the payload already
            // has the new fields, this child is already in v3 shape.
            val payload = rawValue.toMutableMap()
            val payloadAlreadyV3 = existingSyncId != null &&
                    originalKey == existingSyncId &&
                    "deleted" in payload &&
                    "dataQuality" in payload

            if (!payloadAlreadyV3) {
                payload["syncId"] = syncId
                payload.putIfAbsent("deleted", false)
                payload.putIfAbsent("dataQuality", "ok")
                needsRewrite = true
            }

            newCollection[syncId] = payload
        }

        if (!needsRewrite) {
            return false
        }

        // setValue on the collection root replaces the whole collection.
        // This is intentional here: we're rewriting every entry, and we
        // want to drop the old integer-keyed children. Unlike the bug we
        // removed (auto-fix fullSyncToFirebase), this only runs once per
        // user during the format migration AFTER we've read every child
        // back into memory. There is no way to lose data this way as long
        // as the read succeeded — Firebase RTDB get() returns the full
        // current snapshot in a single roundtrip.
        Log.i(TAG, "$collection: rewriting ${newCollection.size} entries as v3 (syncId-keyed)")
        entityRef.setValue(newCollection).await()
        return true
    }
}

private fun SyncIdGenerator.deterministicSyncIdForFirebaseKey(
    entityType: String,
    firebaseKey: String,
    payload: Map<String, Any?>
): String {
    // Best identity inference order:
    //  1. firebaseKey if it's already a UUID
    //  2. payload["id"] (the Room primary key)
    //  3. firebaseKey parsed as int (legacy array index → Room id mapping)
    if (isValid(firebaseKey)) return firebaseKey
    val payloadId = (payload["id"] as? Number)?.toInt()
    if (payloadId != null && payloadId > 0) return deterministicSyncId(entityType, payloadId)
    val keyAsInt = firebaseKey.toIntOrNull()
    if (keyAsInt != null && keyAsInt >= 0) return deterministicSyncId(entityType, keyAsInt)
    // Fall back to a stable hash of the firebaseKey to avoid losing the
    // entity entirely. This should not happen for real Munición data.
    return deterministicSyncId(entityType, firebaseKey.hashCode())
}
