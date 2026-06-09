package al.ahgitdevelopment.municion.ui.forms.compra

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase

/**
 * Compra form state. The purchase is always tied to a parent [Guia] (via [guiaId], which becomes
 * [Compra.idPosGuia]). Image upload is deferred — [imagePath]/[fotoUrl]/[storagePath] are preserved
 * across edits.
 *
 * Spanish law: ammunition bought IN A STORE counts against the annual quota; ammunition bought AT
 * THE SHOOTING RANGE ("campo de tiro") does not (consumed on site). [excedeCupo] respects this.
 */
data class CompraFormState(
    val calibre1: String = "",
    val calibre2: String = "",
    val showCalibre2: Boolean = false,
    val marca: String = "",
    val tipo: String = "",
    val peso: String = "",
    val unidades: String = "",
    val precio: String = "",
    val fecha: String = "",
    val tienda: String = "",
    val valoracion: Float = 0f,
    // Parent guía + quota context
    val guiaId: Int = 0,
    val guiaSyncId: String? = null,
    val cupoDisponible: Int = 0,
    val cupoTotal: Int = 0,
    // Edit metadata / passthrough
    val compraId: Int = 0,
    val syncId: String = "",
    val imagePath: String? = null,
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val isEditing: Boolean = false,
    // Validation errors
    val calibre1Error: String? = null,
    val marcaError: String? = null,
    val tipoError: String? = null,
    val pesoError: String? = null,
    val unidadesError: String? = null,
    val precioError: String? = null,
    val fechaError: String? = null,
    val tiendaError: String? = null,
) {
    /** Whether this purchase is at the shooting range (does not consume quota). */
    val isCompraCampoTiro: Boolean get() = CreateCompraUseCase.isCompraCampoTiro(tienda)

    /**
     * Whether the entered units exceed the available quota. Shooting-range purchases never exceed
     * the quota because they do not count against it.
     */
    val excedeCupo: Boolean
        get() {
            if (isCompraCampoTiro) return false
            val unidadesInt = unidades.toIntOrNull() ?: 0
            return unidadesInt > cupoDisponible
        }

    fun toCompra(): Compra = Compra(
        id = compraId,
        idPosGuia = guiaId,
        calibre1 = calibre1,
        calibre2 = if (showCalibre2) calibre2.ifBlank { null } else null,
        unidades = unidades.toIntOrNull() ?: 0,
        precio = precio.replace(",", ".").toDoubleOrNull() ?: 0.0,
        fecha = fecha,
        tipo = tipo,
        peso = peso.toIntOrNull() ?: 0,
        marca = marca,
        tienda = tienda.ifBlank { null },
        valoracion = valoracion,
        imagePath = imagePath,
        fotoUrl = fotoUrl,
        storagePath = storagePath,
        syncId = syncId,
        guiaSyncId = guiaSyncId,
    )

    companion object {
        /** Initial state for editing an existing purchase, with quota context from the parent guía. */
        fun fromCompra(compra: Compra, guia: Guia): CompraFormState = CompraFormState(
            compraId = compra.id,
            syncId = compra.syncId,
            guiaId = guia.id,
            guiaSyncId = compra.guiaSyncId ?: guia.syncId,
            calibre1 = compra.calibre1,
            calibre2 = compra.calibre2 ?: "",
            showCalibre2 = !compra.calibre2.isNullOrBlank(),
            marca = compra.marca,
            tipo = compra.tipo,
            peso = compra.peso.toString(),
            unidades = compra.unidades.toString(),
            precio = compra.precio.toString(),
            fecha = compra.fecha,
            tienda = compra.tienda ?: "",
            valoracion = compra.valoracion,
            imagePath = compra.imagePath,
            fotoUrl = compra.fotoUrl,
            storagePath = compra.storagePath,
            // When editing, only add back the original units if they counted against the quota.
            // Shooting-range purchases never subtracted, so there is nothing to recover.
            cupoDisponible = guia.disponible() +
                if (CreateCompraUseCase.isCompraCampoTiro(compra.tienda)) 0 else compra.unidades,
            cupoTotal = guia.cupo,
            isEditing = true,
        )

        /** Initial empty state for a new purchase, prefilled from the parent guía. */
        fun fromGuia(guia: Guia, fecha: String): CompraFormState = CompraFormState(
            guiaId = guia.id,
            guiaSyncId = guia.syncId,
            calibre1 = guia.calibre1,
            calibre2 = guia.calibre2 ?: "",
            showCalibre2 = !guia.calibre2.isNullOrBlank(),
            fecha = fecha,
            cupoDisponible = guia.disponible(),
            cupoTotal = guia.cupo,
            isEditing = false,
        )
    }
}
