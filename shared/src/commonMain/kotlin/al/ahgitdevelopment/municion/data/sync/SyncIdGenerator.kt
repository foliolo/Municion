@file:OptIn(ExperimentalUuidApi::class)

package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.util.md5
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Generates stable, globally-unique sync identifiers for entities.
 *
 *  - [newSyncId]: random UUID v4 for brand-new entities created locally.
 *  - [deterministicSyncId]: UUID v3 (MD5) derived from `"municion:v1:<table>:<legacyId>"`,
 *    so two devices that share the same legacy entity converge on the same syncId.
 *
 * The v3 path mirrors `java.util.UUID.nameUUIDFromBytes` exactly (via the multiplatform
 * [md5]) so ids stay byte-identical to those the Android app already generated.
 */
object SyncIdGenerator {

    private const val NAMESPACE = "municion:v1:"
    private val HEX = "0123456789abcdef".toCharArray()

    fun newSyncId(): String = Uuid.random().toString()

    fun deterministicSyncId(table: String, legacyId: Int): String {
        require(table.isNotBlank()) { "table must not be blank" }
        val seed = "$NAMESPACE$table:$legacyId"
        val bytes = md5(seed.encodeToByteArray())
        bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x30).toByte() // version 3
        bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte() // RFC 4122 variant
        return formatUuid(bytes)
    }

    fun isValid(syncId: String?): Boolean {
        if (syncId.isNullOrBlank()) return false
        return runCatching { Uuid.parse(syncId) }.isSuccess
    }

    private fun formatUuid(b: ByteArray): String {
        val sb = StringBuilder(36)
        for (i in 0 until 16) {
            if (i == 4 || i == 6 || i == 8 || i == 10) sb.append('-')
            val v = b[i].toInt() and 0xff
            sb.append(HEX[v ushr 4])
            sb.append(HEX[v and 0x0f])
        }
        return sb.toString()
    }
}
