package al.ahgitdevelopment.municion.ui.navigation.navtypes

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada
import android.os.Bundle
import android.os.Parcelable
import android.util.Base64
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
 * IMPORTANTE: Usamos Base64 para codificar el JSON en navegación.
 * Esto evita problemas con URLs de Firebase Storage que contienen %2F
 * que se corrompían con Uri.encode/decode (doble codificación).
 *
 * @since v3.2.2 (NavType Architecture Migration)
 * @since v3.2.5 (Base64 encoding to fix Firebase URL corruption)
 */

/**
 * JSON configurado para navegación type-safe.
 */
@PublishedApi
internal val navJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

inline fun <reified T : Any> createJsonNavType(isNullableAllowed: Boolean = true): NavType<T?> {
    return object : NavType<T?>(isNullableAllowed) {
        override fun get(bundle: Bundle, key: String): T? {
            @Suppress("DEPRECATION")
            return bundle.getParcelable(key) as? T
        }

        override fun parseValue(value: String): T? {
            return if (value == "null" || value.isEmpty()) {
                null
            } else {
                try {
                    // Decodificar Base64 → JSON → Objeto
                    val jsonBytes = Base64.decode(value, Base64.URL_SAFE or Base64.NO_WRAP)
                    val json = String(jsonBytes, Charsets.UTF_8)
                    navJson.decodeFromString<T>(json)
                } catch (e: Exception) {
                    // Fallback: intentar parsear como JSON directo (compatibilidad)
                    try {
                        navJson.decodeFromString<T>(value)
                    } catch (e2: Exception) {
                        null
                    }
                }
            }
        }

        override fun put(bundle: Bundle, key: String, value: T?) {
            bundle.putParcelable(key, value as? Parcelable)
        }

        override fun serializeAsValue(value: T?): String {
            return if (value == null) {
                "null"
            } else {
                // Objeto → JSON → Base64 (URL-safe, sin padding problemático)
                val json = navJson.encodeToString(value)
                Base64.encodeToString(
                    json.toByteArray(Charsets.UTF_8),
                    Base64.URL_SAFE or Base64.NO_WRAP
                )
            }
        }
    }
}

val LicenciaNavType: NavType<Licencia?> = createJsonNavType()
val GuiaNavType: NavType<Guia?> = createJsonNavType()
val CompraNavType: NavType<Compra?> = createJsonNavType()
val TiradaNavType: NavType<Tirada?> = createJsonNavType()
