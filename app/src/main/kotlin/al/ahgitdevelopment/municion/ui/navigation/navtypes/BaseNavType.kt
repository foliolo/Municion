package al.ahgitdevelopment.municion.ui.navigation.navtypes

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

// TODO delete this class if not needed
/**
 * Base class for custom NavTypes with validation and error handling.
 *
 * Inspirado en FamilyFilmApp pero adaptado para Navigation Compose 2.9+.
 * Maneja serialización type-safe con validación robusta.
 *
 * @param T Tipo de entidad Parcelable (Room entity)
 * @param isNullableAllowed Si permite valores null
 * @param serializer KSerializer para kotlinx.serialization
 * @param type KClass del tipo T
 *
 * @since v3.2.2 (NavType Architecture Migration)
 */
abstract class BaseNavType<T : Parcelable>(
    isNullableAllowed: Boolean = true,
    private val serializer: KSerializer<T>,
    private val type: KClass<T>
) : NavType<T>(isNullableAllowed) {

    /**
     * JSON configurado para navegación:
     * - ignoreUnknownKeys: Tolerante a cambios de schema
     * - encodeDefaults: Incluye valores por defecto
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = false  // Strict parsing
    }

    /**
     * Serializa objeto T a String JSON para navegación.
     * Soporta valores null cuando isNullableAllowed = true.
     */
    override fun serializeAsValue(value: T): String {
        // Handle null values for nullable NavTypes
        @Suppress("UNCHECKED_CAST")
        val nullableValue = value as T? ?: return "null"

        return try {
            json.encodeToString(serializer, value)
        } catch (e: SerializationException) {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("entity_type", type.simpleName ?: "Unknown")
                setCustomKey("error_type", "SerializationError")
                recordException(e)
            }
            throw IllegalArgumentException(
                "Failed to serialize ${type.simpleName}: ${e.message}",
                e
            )
        }
    }

    /**
     * Parsea String JSON a objeto T.
     * Con validación y error handling robusto.
     * Soporta string "null" cuando isNullableAllowed = true.
     */
    override fun parseValue(value: String): T {
        // Handle "null" string for nullable NavTypes
        if (value == "null") {
            @Suppress("UNCHECKED_CAST")
            return null as T
        }

        return try {
            val parsed = json.decodeFromString(serializer, value)

            // Validación custom post-deserialización
            validateEntity(parsed)

            parsed
        } catch (e: SerializationException) {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("entity_type", type.simpleName ?: "Unknown")
                setCustomKey("error_type", "DeserializationError")
                setCustomKey("raw_value_length", value.length)
                recordException(e)
            }
            throw IllegalArgumentException(
                "Failed to parse ${type.simpleName}: ${e.message}",
                e
            )
        } catch (e: IllegalStateException) {
            // Errores de validación (require() en init blocks)
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("entity_type", type.simpleName ?: "Unknown")
                setCustomKey("error_type", "ValidationError")
                recordException(e)
            }
            throw e
        }
    }

    /**
     * Método para validaciones adicionales post-deserialización
     * Override en subclases si necesitas validación específica
     */
    protected open fun validateEntity(entity: T) {
        // Validación base: todas las entidades tienen init {} con require()
        // Las validaciones ya ocurren en constructor
        // Aquí podemos agregar validaciones extra si es necesario
    }

    /**
     * Get from Bundle (Parcelable)
     */
    override fun get(bundle: Bundle, key: String): T? {
        return bundle.getParcelable(key)
    }

    /**
     * Put to Bundle (Parcelable)
     */
    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putParcelable(key, value)
    }

    companion object {
        /**
         * Helper para logging de errores de navegación
         */
        fun logNavigationError(
            entityType: String,
            errorType: String,
            exception: Exception
        ) {
            FirebaseCrashlytics.getInstance().apply {
                setCustomKey("navigation_error", true)
                setCustomKey("entity_type", entityType)
                setCustomKey("error_type", errorType)
                recordException(exception)
            }
        }
    }
}
