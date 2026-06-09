package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import al.ahgitdevelopment.municion.data.sync.dto.CompraDto
import al.ahgitdevelopment.municion.data.sync.dto.GuiaDto
import al.ahgitdevelopment.municion.data.sync.dto.LicenciaDto
import al.ahgitdevelopment.municion.data.sync.dto.TiradaDto
import dev.gitlive.firebase.database.DataSnapshot
import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first

/**
 * The only component that talks to Firebase Realtime Database (via GitLive).
 *
 * Writes: typed entity serialization (tombstones are upserts with `deleted = true`; we never
 * `removeValue` so other devices see the tombstone rather than "entity missing"). Reads: typed
 * DTO deserialization per child (GitLive bridges platform number types — a raw `Map<String, Any?>`
 * read would surface `NSNumber` on iOS). Each malformed child is returned as `(key, null)`.
 *
 * Data lives under `users/{userId}/db/{collection}/{syncId}`.
 */
open class MunicionRtdbDatasource(
    private val database: FirebaseDatabase,
) {
    private fun collection(
        userId: String,
        path: String,
    ) = database.reference("users/$userId/db/$path")

    // ---- Writes (per entity; tombstones included) -------------------------------------------

    open suspend fun writeLicencia(
        userId: String,
        entity: Licencia,
    ) {
        collection(userId, "licencias").child(entity.syncId).setValue(entity) { encodeDefaults = true }
    }

    open suspend fun writeGuia(
        userId: String,
        entity: Guia,
    ) {
        collection(userId, "guias").child(entity.syncId).setValue(entity) { encodeDefaults = true }
    }

    open suspend fun writeCompra(
        userId: String,
        entity: Compra,
    ) {
        collection(userId, "compras").child(entity.syncId).setValue(entity) { encodeDefaults = true }
    }

    open suspend fun writeTirada(
        userId: String,
        entity: Tirada,
    ) {
        collection(userId, "tiradas").child(entity.syncId).setValue(entity) { encodeDefaults = true }
    }

    // ---- Reads (typed DTO per child; malformed children become (key, null)) ------------------

    open suspend fun readLicencias(userId: String): List<Pair<String?, LicenciaDto?>> =
        readChildren(userId, "licencias") { it.value(LicenciaDto.serializer()) }

    open suspend fun readGuias(userId: String): List<Pair<String?, GuiaDto?>> =
        readChildren(userId, "guias") { it.value(GuiaDto.serializer()) }

    open suspend fun readCompras(userId: String): List<Pair<String?, CompraDto?>> =
        readChildren(userId, "compras") { it.value(CompraDto.serializer()) }

    open suspend fun readTiradas(userId: String): List<Pair<String?, TiradaDto?>> =
        readChildren(userId, "tiradas") { it.value(TiradaDto.serializer()) }

    private suspend fun <T> readChildren(
        userId: String,
        path: String,
        deserialize: (DataSnapshot) -> T,
    ): List<Pair<String?, T?>> {
        val snapshot = collection(userId, path).valueEvents.first()
        return snapshot.children
            .map { child ->
                child.key to runCatching { deserialize(child) }.getOrNull()
            }.toList()
    }
}
