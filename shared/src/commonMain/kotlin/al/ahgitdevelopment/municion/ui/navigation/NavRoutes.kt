package al.ahgitdevelopment.municion.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes.
 *
 * KMP migration note: routes carry the entity **id** (a primitive that Navigation Compose
 * serializes natively), not the full Parcelable entity. Form screens load the entity from the
 * repository by id. This removes the Android-only Parcelable/Base64 custom NavType machinery
 * (no custom NavType / typeMap / navigateSafely needed).
 */
sealed interface Route

// ----- Auth -----
@Serializable
data object Login : Route

@Serializable
data object Migration : Route

// ----- Main tabs -----
@Serializable
data object Licencias : Route

@Serializable
data object Guias : Route

@Serializable
data object Compras : Route

@Serializable
data object Tiradas : Route

@Serializable
data object Settings : Route

// ----- Forms (id == null → create) -----
@Serializable
data class LicenciaForm(val licenciaId: Int? = null) : Route

/** Create needs [tipoLicencia] (selected via dialog); edit loads by [guiaId]. */
@Serializable
data class GuiaForm(val guiaId: Int? = null, val tipoLicencia: Int = -1) : Route

/** [guiaId] is the parent guía (always required, for quota validation). */
@Serializable
data class CompraForm(val compraId: Int? = null, val guiaId: Int) : Route

@Serializable
data class TiradaForm(val tiradaId: Int? = null) : Route
