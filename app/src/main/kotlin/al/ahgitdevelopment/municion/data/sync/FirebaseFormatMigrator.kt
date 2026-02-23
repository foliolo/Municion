package al.ahgitdevelopment.municion.data.sync

import android.content.Context
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Migrates Firebase RTDB data from array format (indexed by 0,1,2...)
 * to map format (keyed by entity ID).
 *
 * Array format (legacy):
 * ```
 * guias: [ {id:1, marca:"S&W"}, {id:2, marca:"Tikka"} ]
 * ```
 * Firebase keys: "0", "1"
 *
 * Map format (new):
 * ```
 * guias: { "1": {id:1, marca:"S&W"}, "2": {id:2, marca:"Tikka"} }
 * ```
 * Firebase keys: "1", "2" (entity IDs)
 *
 * Detection: If a child key is "0", it's array format (Room auto-increment IDs start at 1).
 * Migration is idempotent: if already migrated or empty, no-op.
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
        private const val FORMAT_VERSION_MAP = 2
        private val ENTITY_PATHS = listOf("guias", "compras", "licencias", "tiradas")
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Attempts to migrate all 4 entity collections from array to map format.
     *
     * @param userId The authenticated user's UID
     * @return true if migration was performed or already done, false on error
     */
    suspend fun migrateIfNeeded(userId: String): Boolean {
        // Check local flag first to avoid unnecessary Firebase reads
        if (prefs.getInt(PREF_KEY_FORMAT_VERSION, 0) >= FORMAT_VERSION_MAP) {
            Log.d(TAG, "Already migrated (local flag)")
            return true
        }

        return try {
            val dbRef = firebaseDb.child("users").child(userId).child("db")

            // Check Firebase meta flag
            val metaSnapshot = dbRef.child("_meta").child("format_version").get().await()
            val remoteVersion = (metaSnapshot.value as? Number)?.toInt() ?: 0
            if (remoteVersion >= FORMAT_VERSION_MAP) {
                Log.d(TAG, "Already migrated (remote flag)")
                prefs.edit { putInt(PREF_KEY_FORMAT_VERSION, FORMAT_VERSION_MAP) }
                return true
            }

            // Migrate each entity collection
            var migrated = false
            for (entityPath in ENTITY_PATHS) {
                val entityRef = dbRef.child(entityPath)
                val snapshot = entityRef.get().await()

                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    Log.d(TAG, "$entityPath: empty, skipping")
                    continue
                }

                // Detect format: key "0" means array format
                val isArrayFormat = snapshot.children.any { it.key == "0" }
                if (!isArrayFormat) {
                    Log.d(TAG, "$entityPath: already map format")
                    continue
                }

                Log.i(TAG, "$entityPath: migrating array → map (${snapshot.childrenCount} items)")

                // Build map keyed by entity ID
                val entityMap = mutableMapOf<String, Any?>()
                snapshot.children.forEach { child ->
                    val map = child.value as? Map<*, *> ?: return@forEach
                    val id = (map["id"] as? Number)?.toInt()
                    if (id != null && id > 0) {
                        entityMap[id.toString()] = child.value
                    } else {
                        // Fallback: use the array index + 1 as key
                        val fallbackKey = ((child.key?.toIntOrNull() ?: 0) + 1).toString()
                        entityMap[fallbackKey] = child.value
                        Log.w(TAG, "$entityPath: item with key=${child.key} has no valid ID, using fallback key=$fallbackKey")
                    }
                }

                // Atomic write: replace the entire collection
                entityRef.setValue(entityMap).await()
                migrated = true
                Log.i(TAG, "$entityPath: migrated ${entityMap.size} items to map format")
            }

            // Save format version flag
            dbRef.child("_meta").child("format_version").setValue(FORMAT_VERSION_MAP).await()
            prefs.edit { putInt(PREF_KEY_FORMAT_VERSION, FORMAT_VERSION_MAP) }

            if (migrated) {
                Log.i(TAG, "Firebase format migration completed successfully")
                crashlytics.log("Firebase array→map migration completed")
            } else {
                Log.d(TAG, "No migration needed (all collections already map format or empty)")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
            crashlytics.log("Firebase format migration failed: ${e.message}")
            crashlytics.recordException(e)
            // Non-fatal: the dual-read approach in syncFromFirebase handles both formats
            false
        }
    }
}
