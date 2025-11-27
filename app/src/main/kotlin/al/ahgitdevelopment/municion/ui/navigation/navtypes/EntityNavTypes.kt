package al.ahgitdevelopment.municion.ui.navigation.navtypes

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Custom NavTypes para las 4 entidades principales de Munición.
 *
 * Cada NavType:
 * - Maneja serialización JSON type-safe
 * - Valida datos durante deserialización
 * - Reporta errores a Crashlytics
 * - Soporta valores nullable (para formularios de creación)
 *
 * @since v3.3.0 (NavType Architecture Migration)
 */

inline fun <reified T : Any> createJsonNavType(isNullableAllowed: Boolean = true): NavType<T?> {
    return object : NavType<T?>(isNullableAllowed) {
        override fun get(bundle: Bundle, key: String): T? {
            @Suppress("DEPRECATION")
            return bundle.getParcelable(key)
        }

        override fun parseValue(value: String): T? {
            return if (value == "null") {
                null
            } else {
                Json.decodeFromString<T>(value)
            }
        }

        override fun put(bundle: Bundle, key: String, value: T?) {
            bundle.putParcelable(key, value as? android.os.Parcelable)
        }

        override fun serializeAsValue(value: T?): String {
            return if (value == null) {
                "null"
            } else {
                Json.encodeToString(value)
            }
        }
    }
}


val LicenciaNavType: NavType<Licencia?> = createJsonNavType()
val GuiaNavType: NavType<Guia?> = createJsonNavType()
val CompraNavType: NavType<Compra?> = createJsonNavType()
val TiradaNavType: NavType<Tirada?> = createJsonNavType()
