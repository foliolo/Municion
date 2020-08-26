package al.ahgitdevelopment.municion.ui.licencias.types

abstract class BaseLicense(
    val licenseType: LicenseType,
    val licenseNumber: String,
    val dateIssue: String
) {
    constructor() : this(
        licenseType = LicenseType.A_profesionales,
        licenseNumber = "",
        dateIssue = ""
    )
}

enum class LicenseType {
    A_profesionales,
    B_defensa,
    C_viginlantes_seguridad,
    D_caza_mayor,
    E_escopeta,
    F_tiro_olimpico,
    AE_autorizacion_especial,
    AER_autorizacion_replica,
    Libro_coleccionista,
    Autonomica_caza,
    Autonomica_pesca,
    Federativa_tiro,
    Permiso_conducir;

    companion object {

        fun valueOf(value: Int?): LicenseType {
            return when (value) {
                0 -> A_profesionales
                1 -> B_defensa
                2 -> C_viginlantes_seguridad
                3 -> D_caza_mayor
                4 -> E_escopeta
                5 -> F_tiro_olimpico
                6 -> AE_autorizacion_especial
                7 -> AER_autorizacion_replica
                8 -> Libro_coleccionista
                9 -> Autonomica_caza
                10 -> Autonomica_pesca
                11 -> Federativa_tiro
                12 -> Permiso_conducir
                else -> A_profesionales
            }
        }
    }
}
