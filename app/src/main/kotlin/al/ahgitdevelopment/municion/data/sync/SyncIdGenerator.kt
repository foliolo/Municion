package al.ahgitdevelopment.municion.data.sync

import java.util.UUID

/**
 * Generates stable, globally-unique sync identifiers for entities.
 *
 * Two strategies:
 *
 *  - [newSyncId]: random UUID v4 for brand-new entities created locally.
 *
 *  - [deterministicSyncId]: UUID v3-style identifier derived from
 *    `"municion:v1:<table>:<legacyId>"`. Two devices that share the same
 *    pre-syncId entity (same Room/legacy id) converge on the same syncId,
 *    so the post-migration sync does not create duplicates.
 *
 * The "municion:v1:" prefix prevents collisions with any other system that
 * happens to use [UUID.nameUUIDFromBytes] on table+id pairs.
 */
object SyncIdGenerator {

    private const val NAMESPACE = "municion:v1:"

    fun newSyncId(): String = UUID.randomUUID().toString()

    fun deterministicSyncId(table: String, legacyId: Int): String {
        require(table.isNotBlank()) { "table must not be blank" }
        val seed = "$NAMESPACE$table:$legacyId"
        return UUID.nameUUIDFromBytes(seed.toByteArray(Charsets.UTF_8)).toString()
    }

    fun isValid(syncId: String?): Boolean {
        if (syncId.isNullOrBlank()) return false
        return try {
            UUID.fromString(syncId)
            true
        } catch (_: IllegalArgumentException) {
            false
        }
    }
}
