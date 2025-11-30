package al.ahgitdevelopment.municion.ui.navigation.navtypes

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Type map global para navegación type-safe en Munición.
 *
 * Registra todos los custom NavTypes para que Navigation Compose
 * los use automáticamente durante serialización/deserialización.
 *
 * Uso:
 * ```
 * NavHost(
 *     navController = navController,
 *     startDestination = Licencias,
 *     typeMap = municionTypeMap
 * ) {
 *     composable<LicenciaForm>(typeMap = municionTypeMap) { ... }
 * }
 * ```
 *
 * @since v3.3.0 (NavType Architecture Migration)
 */
val municionTypeMap: Map<KType, NavType<*>> = mapOf(
    // Licencia (nullable y non-nullable)
    typeOf<Licencia?>() to LicenciaNavType,
    typeOf<Licencia>() to LicenciaNavType,

    // Guia (nullable y non-nullable)
    typeOf<Guia?>() to GuiaNavType,
    typeOf<Guia>() to GuiaNavType,

    // Compra (nullable y non-nullable)
    typeOf<Compra?>() to CompraNavType,
    typeOf<Compra>() to CompraNavType,

    // Tirada (nullable y non-nullable)
    typeOf<Tirada?>() to TiradaNavType,
    typeOf<Tirada>() to TiradaNavType
)

/**
 * Extension function para navegar con validación automática
 *
 * Envuelve navigate() con try-catch para manejar errores de serialización.
 * Los errores ya son reportados a Crashlytics por los NavTypes.
 *
 * @param T Route type (debe ser @Serializable)
 * @param route Ruta de destino
 */
inline fun <reified T : Any> NavController.navigateSafely(route: T) {
    try {
        this.navigate(route)
    } catch (e: IllegalArgumentException) {
        // Error de serialización - ya reportado a Crashlytics por NavType
        Log.e("MunicionNavigation", "Failed to navigate to ${T::class.simpleName}", e)
        // Opcional: mostrar snackbar al usuario
    }
}
