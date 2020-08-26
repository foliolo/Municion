package al.ahgitdevelopment.municion.ui.licencias.types

class CazaLicense(
    val expiryDate: String,
    val subscriberNumber: String,
    val insuranceNumber: String,
    val cityRegion: Int
) : BaseLicense()