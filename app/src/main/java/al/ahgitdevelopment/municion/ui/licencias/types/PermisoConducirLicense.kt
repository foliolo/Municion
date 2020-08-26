package al.ahgitdevelopment.municion.ui.licencias.types

class PermisoConducirLicense(
    val expiryDate: String,
    val age: String,
    val insuranceNumber: String,
    val drivingLicenseType: Int
) : BaseLicense()