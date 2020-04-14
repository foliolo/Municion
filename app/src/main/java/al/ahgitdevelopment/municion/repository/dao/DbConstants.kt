package al.ahgitdevelopment.municion.repository.dao

// Table Names
const val TABLE_GUIAS = "guias"
const val TABLE_COMPRAS = "compras"
const val TABLE_LICENCIAS = "licencias"
const val TABLE_TIRADAS = "tiradas"

// Common column names
const val KEY_ID = "id"

// Table GUIAS  - column names
const val KEY_GUIA_ID_COMPRA = "id_compra"
const val KEY_GUIA_ID_LICENCIA = "id_licencia"
const val KEY_GUIA_APODO = "apodo"
const val KEY_GUIA_MARCA = "marca"
const val KEY_GUIA_MODELO = "modelo"
const val KEY_GUIA_TIPO_ARMA = "tipo_arma"
const val KEY_GUIA_CALIBRE1 = "calibre1"
const val KEY_GUIA_CALIBRE2 = "calibre2"
const val KEY_GUIA_NUM_GUIA = "num_guia"
const val KEY_GUIA_NUM_ARMA = "num_arma"
const val KEY_GUIA_IMAGEN = "imagen_uri"
const val KEY_GUIA_CUPO = "cupo"
const val KEY_GUIA_GASTADO = "gastado"

// Table COMPRAS  - column names
const val KEY_COMPRA_ID_POS_GUIA = "idPosGuia"
const val KEY_COMPRA_CALIBRE1 = "calibre1"
const val KEY_COMPRA_CALIBRE2 = "calibre2"
const val KEY_COMPRA_UNIDADES = "unidades"
const val KEY_COMPRA_PRECIO = "precio"
const val KEY_COMPRA_FECHA = "fecha"
const val KEY_COMPRA_TIPO = "tipo"
const val KEY_COMPRA_PESO = "peso"
const val KEY_COMPRA_MARCA = "marca"
const val KEY_COMPRA_TIENDA = "tienda"
const val KEY_COMPRA_IMAGEN = "imagen_uri"
const val KEY_COMPRA_VALORACION = "valoracion"

// Table LICENCIAS  - column names
const val KEY_LICENCIAS_TIPO = "tipo"
const val KEY_LICENCIAS_NOMBRE = "nombre"
const val KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION = "tipo_permiso_conduccion"
const val KEY_LICENCIAS_EDAD = "edad"
const val KEY_LICENCIAS_FECHA_EXPEDICION = "fecha_expedicion"
const val KEY_LICENCIAS_FECHA_CADUCIDAD = "fecha_caducidad"
const val KEY_LICENCIAS_NUM_LICENCIA = "num_licencia"
const val KEY_LICENCIAS_NUM_ABONADO = "num_abonado"
const val KEY_LICENCIAS_NUM_SEGURO = "num_seguro"
const val KEY_LICENCIAS_AUTONOMIA = "autonomia"
const val KEY_LICENCIAS_ESCALA = "escala"
const val KEY_LICENCIAS_CATEGORIA = "categoria"

// Table TIRADAS  - column names
const val KEY_TIRADAS_DESCRIPCION = "descripcion"
const val KEY_TIRADAS_RANGO = "rango"
const val KEY_TIRADAS_FECHA = "fecha"
const val KEY_TIRADAS_PUNTUACION = "puntuacion"

// Database Version
const val DATABASE_VERSION = 22

// Database Name
const val DATABASE_NAME = "DBMunicion.db"