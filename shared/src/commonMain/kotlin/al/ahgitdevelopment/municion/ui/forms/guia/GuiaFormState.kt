package al.ahgitdevelopment.municion.ui.forms.guia

import al.ahgitdevelopment.municion.data.local.room.entities.Guia

/**
 * Guía form state. The annual quota (cupo) can be auto-derived from the weapon type unless the
 * user opts into a custom value. Image upload is deferred to phase 7 — [imagePath]/[fotoUrl]/
 * [storagePath] are preserved across edits and passed through unchanged in [toGuia].
 */
data class GuiaFormState(
    val marca: String = "",
    val modelo: String = "",
    val apodo: String = "",
    val tipoArma: Int = 0,
    val calibre1: String = "",
    val calibre2: String = "",
    val showCalibre2: Boolean = false,
    val numGuia: String = "",
    val numArma: String = "",
    val cupo: String = "",
    val gastado: String = "0",
    val customCupo: Boolean = false,
    // Edit metadata / passthrough
    val guiaId: Int = 0,
    val syncId: String = "",
    val tipoLicencia: Int = 0,
    val imagePath: String? = null,
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val isEditing: Boolean = false,
    // Validation errors
    val marcaError: String? = null,
    val modeloError: String? = null,
    val apodoError: String? = null,
    val calibre1Error: String? = null,
    val numGuiaError: String? = null,
    val numArmaError: String? = null,
    val cupoError: String? = null,
) {
    /** Spent ammunition is only editable when modifying an existing guía. */
    val showGastado: Boolean get() = isEditing

    fun toGuia(): Guia = Guia(
        id = guiaId,
        tipoLicencia = tipoLicencia,
        marca = marca,
        modelo = modelo,
        apodo = apodo,
        tipoArma = tipoArma,
        calibre1 = calibre1,
        calibre2 = if (showCalibre2) calibre2.ifBlank { null } else null,
        numGuia = numGuia,
        numArma = numArma,
        cupo = cupo.toIntOrNull() ?: 0,
        gastado = gastado.toIntOrNull() ?: 0,
        imagePath = imagePath,
        fotoUrl = fotoUrl,
        storagePath = storagePath,
        syncId = syncId,
    )

    companion object {
        fun fromGuia(g: Guia): GuiaFormState = GuiaFormState(
            guiaId = g.id,
            syncId = g.syncId,
            tipoLicencia = g.tipoLicencia,
            marca = g.marca,
            modelo = g.modelo,
            apodo = g.apodo,
            tipoArma = g.tipoArma,
            calibre1 = g.calibre1,
            calibre2 = g.calibre2 ?: "",
            showCalibre2 = !g.calibre2.isNullOrBlank(),
            numGuia = g.numGuia,
            numArma = g.numArma,
            cupo = g.cupo.toString(),
            gastado = g.gastado.toString(),
            imagePath = g.imagePath,
            fotoUrl = g.fotoUrl,
            storagePath = g.storagePath,
            isEditing = true,
        )
    }
}
