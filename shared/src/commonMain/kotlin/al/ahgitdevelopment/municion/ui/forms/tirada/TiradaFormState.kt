package al.ahgitdevelopment.municion.ui.forms.tirada

import al.ahgitdevelopment.municion.data.local.room.entities.Tirada

/**
 * Tirada form state. No image handling for this entity. [modalidad]/[categoria] are stored as the
 * selected array string directly (not an index). [syncId] is preserved across the
 * fromTirada → toTirada round-trip.
 */
data class TiradaFormState(
    val descripcion: String = "",
    val localizacion: String = "",
    val categoria: String = "",
    val modalidad: String = Tirada.MODALIDAD_PRECISION,
    val fecha: String = "",
    val puntuacion: String = "0",
    // Edit metadata / passthrough
    val tiradaId: Int = 0,
    val syncId: String = "",
    val isEditing: Boolean = false,
    // Validation errors
    val descripcionError: String? = null,
    val fechaError: String? = null,
) {
    val maxPuntuacion: Int get() = Tirada.getMaxPuntuacion(modalidad)

    fun toTirada(): Tirada = Tirada(
        id = tiradaId,
        descripcion = descripcion,
        localizacion = localizacion.ifBlank { null },
        categoria = categoria.ifBlank { null },
        modalidad = modalidad.ifBlank { null },
        fecha = fecha,
        puntuacion = (puntuacion.toIntOrNull() ?: 0).coerceIn(0, maxPuntuacion),
        syncId = syncId,
    )

    companion object {
        fun fromTirada(t: Tirada): TiradaFormState = TiradaFormState(
            tiradaId = t.id,
            syncId = t.syncId,
            descripcion = t.descripcion,
            localizacion = t.localizacion ?: "",
            categoria = t.categoria ?: "",
            modalidad = t.modalidad ?: Tirada.MODALIDAD_PRECISION,
            fecha = t.fecha,
            puntuacion = t.puntuacion.toString(),
            isEditing = true,
        )
    }
}
