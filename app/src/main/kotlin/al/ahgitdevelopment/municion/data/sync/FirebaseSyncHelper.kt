package al.ahgitdevelopment.municion.data.sync

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic Firebase sync helper that replaces the duplicated sync logic in all 4 repositories.
 *
 * Key improvements over the old approach:
 * - Per-entity writes instead of full-list upload on every operation
 * - Per-entity deletes instead of full-list re-upload
 * - Smart diff on syncFromFirebase (upsert changed, delete removed)
 * - Mutex per entity type to prevent concurrent sync race conditions
 * - Full-list sync only used for auto-fix and manual sync
 */
@Singleton
class FirebaseSyncHelper @Inject constructor(
    private val firebaseDb: DatabaseReference,
    private val crashlytics: FirebaseCrashlytics
) {

    companion object {
        private const val TAG = "FirebaseSyncHelper"
    }

    // Per-entity-type mutexes to prevent concurrent sync operations
    private val mutexes = mutableMapOf<String, Mutex>()

    private fun getMutex(entityPath: String): Mutex {
        return synchronized(mutexes) {
            mutexes.getOrPut(entityPath) { Mutex() }
        }
    }

    /**
     * Writes a single entity to Firebase using its ID as the key.
     *
     * Path: users/{userId}/db/{entityPath}/{entityId}
     *
     * @param userId Authenticated user ID
     * @param entityPath Collection name (e.g., "guias", "compras")
     * @param entityId The entity's ID
     * @param data Entity converted to Firebase map via toFirebaseMap()
     */
    suspend fun writeEntity(
        userId: String,
        entityPath: String,
        entityId: Int,
        data: Map<String, Any?>
    ): Result<Unit> {
        return try {
            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child(entityPath)
                .child(entityId.toString())
                .setValue(data)
                .await()

            Log.d(TAG, "Wrote $entityPath/$entityId to Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write $entityPath/$entityId: ${e.message}", e)
            crashlytics.log("Failed to write $entityPath/$entityId: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a single entity from Firebase.
     *
     * @param userId Authenticated user ID
     * @param entityPath Collection name
     * @param entityId The entity's ID to remove
     */
    suspend fun deleteEntity(
        userId: String,
        entityPath: String,
        entityId: Int
    ): Result<Unit> {
        return try {
            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child(entityPath)
                .child(entityId.toString())
                .removeValue()
                .await()

            Log.d(TAG, "Deleted $entityPath/$entityId from Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete $entityPath/$entityId: ${e.message}", e)
            crashlytics.log("Failed to delete $entityPath/$entityId: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    /**
     * Smart diff-based sync from Firebase to Room.
     *
     * Algorithm:
     * 1. Download all entities from Firebase
     * 2. Parse each one (caller provides parser)
     * 3. Compare with local IDs + timestamps
     * 4. Upsert remote entities that are newer or don't exist locally
     * 5. Delete local entities that no longer exist in Firebase (if Firebase is non-empty)
     *
     * @param userId Authenticated user ID
     * @param entityPath Collection name
     * @param localIds Set of all local entity IDs
     * @param localTimestamps Map of local entity ID -> updatedAt timestamp
     * @param parser Function that parses a Firebase child map into an entity (or null on error)
     * @param upsert Function that upserts a single parsed entity into Room
     * @param deleteLocal Function that deletes a local entity by ID
     * @return DiffSyncResult with counts of operations performed
     */
    suspend fun <T> syncFromFirebaseWithDiff(
        userId: String,
        entityPath: String,
        localIds: Set<Int>,
        localTimestamps: Map<Int, Long>,
        parser: (key: String, value: Any?) -> T?,
        getId: (T) -> Int,
        getUpdatedAt: (T) -> Long,
        upsert: suspend (T) -> Unit,
        deleteLocal: suspend (Int) -> Unit
    ): DiffSyncResult = getMutex(entityPath).withLock {
        try {
            val snapshot = firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child(entityPath)
                .get()
                .await()

            val totalInFirebase = snapshot.childrenCount.toInt()
            var upserted = 0
            var skipped = 0
            var deleted = 0
            var parseErrors = 0
            val remoteIds = mutableSetOf<Int>()

            snapshot.children.forEach { child ->
                val key = child.key ?: "unknown"
                val parsed = parser(key, child.value)
                if (parsed == null) {
                    parseErrors++
                    return@forEach
                }

                val remoteId = getId(parsed)
                val remoteUpdatedAt = getUpdatedAt(parsed)
                remoteIds.add(remoteId)

                val localTimestamp = localTimestamps[remoteId]

                if (localTimestamp == null || remoteUpdatedAt >= localTimestamp) {
                    // Entity doesn't exist locally, or remote is newer/equal → upsert
                    upsert(parsed)
                    upserted++
                } else {
                    // Local is more recent → keep local (will sync on next write)
                    skipped++
                }
            }

            // Delete local entities that no longer exist in Firebase
            // Only if Firebase has data (non-empty = someone may have deleted from another device)
            if (totalInFirebase > 0) {
                val toDelete = localIds - remoteIds
                for (id in toDelete) {
                    deleteLocal(id)
                    deleted++
                }
            }

            Log.i(TAG, "Diff sync $entityPath: total=$totalInFirebase, upserted=$upserted, skipped=$skipped, deleted=$deleted, parseErrors=$parseErrors")

            DiffSyncResult(
                totalInFirebase = totalInFirebase,
                upserted = upserted,
                skipped = skipped,
                deleted = deleted,
                parseErrors = parseErrors
            )
        } catch (e: Exception) {
            Log.e(TAG, "Diff sync $entityPath failed: ${e.message}", e)
            crashlytics.log("Diff sync $entityPath failed: ${e.message}")
            crashlytics.recordException(e)
            DiffSyncResult(totalInFirebase = 0, upserted = 0, skipped = 0, deleted = 0, parseErrors = 0, error = e)
        }
    }

    /**
     * Full sync upload: writes ALL entities to Firebase as a map keyed by ID.
     * Used only for auto-fix (when Firebase is corrupt) and manual sync.
     *
     * @param userId Authenticated user ID
     * @param entityPath Collection name
     * @param entityMaps Map of entity ID (as String) to Firebase map
     */
    suspend fun fullSyncToFirebase(
        userId: String,
        entityPath: String,
        entityMaps: Map<String, Map<String, Any?>>
    ): Result<Unit> = getMutex(entityPath).withLock {
        try {
            firebaseDb
                .child("users")
                .child(userId)
                .child("db")
                .child(entityPath)
                .setValue(entityMaps)
                .await()

            Log.i(TAG, "Full sync uploaded ${entityMaps.size} $entityPath to Firebase")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Full sync $entityPath failed: ${e.message}", e)
            crashlytics.log("Full sync $entityPath failed: ${e.message}")
            crashlytics.recordException(e)
            Result.failure(e)
        }
    }

    data class DiffSyncResult(
        val totalInFirebase: Int,
        val upserted: Int,
        val skipped: Int,
        val deleted: Int,
        val parseErrors: Int,
        val error: Exception? = null
    ) {
        val success: Boolean get() = error == null
    }
}
