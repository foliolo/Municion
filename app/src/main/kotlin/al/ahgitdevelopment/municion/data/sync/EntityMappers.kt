package al.ahgitdevelopment.municion.data.sync

import al.ahgitdevelopment.municion.data.local.room.entities.Compra
import al.ahgitdevelopment.municion.data.local.room.entities.Guia
import al.ahgitdevelopment.municion.data.local.room.entities.Licencia
import al.ahgitdevelopment.municion.data.local.room.entities.Tirada

/**
 * Extension functions to convert Room entities to Firebase-compatible maps.
 *
 * Each entity maps its Room fields to the key names expected by Firebase RTDB.
 * Field names match the property names used in parseAndValidate* methods.
 */

fun Guia.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "syncId" to syncId,
    "idCompra" to idCompra,
    "tipoLicencia" to tipoLicencia,
    "marca" to marca,
    "modelo" to modelo,
    "apodo" to apodo,
    "tipoArma" to tipoArma,
    "calibre1" to calibre1,
    "calibre2" to calibre2,
    "numGuia" to numGuia,
    "numArma" to numArma,
    "cupo" to cupo,
    "gastado" to gastado,
    "imagePath" to imagePath,
    "fotoUrl" to fotoUrl,
    "storagePath" to storagePath,
    "updatedAt" to updatedAt,
    "deleted" to deleted,
    "deletedAt" to deletedAt,
    "dataQuality" to dataQuality
)

fun Compra.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "syncId" to syncId,
    "guiaSyncId" to guiaSyncId,
    "idPosGuia" to idPosGuia,
    "calibre1" to calibre1,
    "calibre2" to calibre2,
    "unidades" to unidades,
    "precio" to precio,
    "fecha" to fecha,
    "tipo" to tipo,
    "peso" to peso,
    "marca" to marca,
    "tienda" to tienda,
    "valoracion" to valoracion,
    "imagePath" to imagePath,
    "fotoUrl" to fotoUrl,
    "storagePath" to storagePath,
    "updatedAt" to updatedAt,
    "deleted" to deleted,
    "deletedAt" to deletedAt,
    "dataQuality" to dataQuality
)

fun Licencia.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "syncId" to syncId,
    "tipo" to tipo,
    "nombre" to nombre,
    "tipoPermisoConduccion" to tipoPermisoConduccion,
    "edad" to edad,
    "fechaExpedicion" to fechaExpedicion,
    "fechaCaducidad" to fechaCaducidad,
    "numLicencia" to numLicencia,
    "numAbonado" to numAbonado,
    "numSeguro" to numSeguro,
    "autonomia" to autonomia,
    "escala" to escala,
    "categoria" to categoria,
    "fotoUrl" to fotoUrl,
    "storagePath" to storagePath,
    "updatedAt" to updatedAt,
    "deleted" to deleted,
    "deletedAt" to deletedAt,
    "dataQuality" to dataQuality
)

/**
 * Tirada uses "rango" in Firebase for compatibility with existing data,
 * mapped from "localizacion" in Room.
 */
fun Tirada.toFirebaseMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "syncId" to syncId,
    "descripcion" to descripcion,
    "rango" to localizacion,  // Firebase uses "rango" for backward compatibility
    "categoria" to categoria,
    "modalidad" to modalidad,
    "fecha" to fecha,
    "puntuacion" to puntuacion,
    "updatedAt" to updatedAt,
    "deleted" to deleted,
    "deletedAt" to deletedAt,
    "dataQuality" to dataQuality
)
