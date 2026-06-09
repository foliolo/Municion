package al.ahgitdevelopment.municion.ui.forms.licencia

import al.ahgitdevelopment.municion.data.local.room.entities.Licencia

/**
 * Licencia form state. Field visibility depends on the licence type. Image upload is deferred
 * to phase 7 — [fotoUrl]/[storagePath] are preserved across edits.
 */
data class LicenciaFormState(
    val tipoLicencia: Int = 0,
    val numLicencia: String = "",
    val fechaExpedicion: String = "",
    val fechaCaducidad: String = "",
    val numAbonado: String = "",
    val numSeguro: String = "",
    val autonomia: Int = 0,
    val tipoPermisoConducir: Int = 0,
    val edad: String = "",
    val escala: Int = 0,
    val categoria: Int = 0,
    // Edit metadata / passthrough
    val licenciaId: Int = 0,
    val syncId: String = "",
    val fotoUrl: String? = null,
    val storagePath: String? = null,
    val isEditing: Boolean = false,
    // Validation errors
    val numLicenciaError: String? = null,
    val fechaExpedicionError: String? = null,
    val numSeguroError: String? = null,
    val edadError: String? = null,
) {
    val showEscala: Boolean get() = tipoLicencia == 0
    val showFechaCaducidad: Boolean get() = tipoLicencia != 0
    val showNumAbonado: Boolean get() = tipoLicencia in 9..11
    val showNumSeguro: Boolean get() = tipoLicencia in 9..10
    val showAutonomia: Boolean get() = tipoLicencia in 9..11
    val showPermisoConducir: Boolean get() = tipoLicencia == 12
    val showEdad: Boolean get() = tipoLicencia == 12
    val showCategoria: Boolean get() = tipoLicencia == 11

    fun toLicencia(): Licencia = Licencia(
        id = licenciaId,
        tipo = tipoLicencia,
        nombre = null,
        tipoPermisoConduccion = if (showPermisoConducir) tipoPermisoConducir else -1,
        edad = edad.toIntOrNull() ?: 30,
        fechaExpedicion = fechaExpedicion,
        fechaCaducidad = fechaCaducidad.ifBlank { "31/12/3000" },
        numLicencia = numLicencia,
        numAbonado = if (showNumAbonado) numAbonado.toIntOrNull() ?: -1 else -1,
        numSeguro = if (showNumSeguro) numSeguro.ifBlank { null } else null,
        autonomia = if (showAutonomia) autonomia else -1,
        escala = if (showEscala) escala else -1,
        categoria = if (showCategoria) categoria else -1,
        fotoUrl = fotoUrl,
        storagePath = storagePath,
        syncId = syncId,
    )

    companion object {
        fun fromLicencia(l: Licencia): LicenciaFormState = LicenciaFormState(
            licenciaId = l.id,
            syncId = l.syncId,
            tipoLicencia = l.tipo,
            numLicencia = l.numLicencia,
            fechaExpedicion = l.fechaExpedicion,
            fechaCaducidad = l.fechaCaducidad,
            numAbonado = l.numAbonado.takeIf { it >= 0 }?.toString() ?: "",
            numSeguro = l.numSeguro ?: "",
            autonomia = l.autonomia.takeIf { it >= 0 } ?: 0,
            tipoPermisoConducir = l.tipoPermisoConduccion.takeIf { it >= 0 } ?: 0,
            edad = l.edad.toString(),
            escala = l.escala.takeIf { it >= 0 } ?: 0,
            categoria = l.categoria.takeIf { it >= 0 } ?: 0,
            fotoUrl = l.fotoUrl,
            storagePath = l.storagePath,
            isEditing = true,
        )
    }
}
