package al.ahgitdevelopment.municion.ui.licencias.types

/**
 * Professional license for military and police
 */
data class AELicense(
    val expiryDate: String
) : BaseLicense()