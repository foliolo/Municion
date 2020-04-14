package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.*
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Alberto on 12/05/2016.
 */
@Entity(tableName = TABLE_COMPRAS)
open class Compra(

        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = KEY_ID) var id: Long,

        @ColumnInfo(name = KEY_COMPRA_ID_POS_GUIA) var idPosGuia: Int,

        @ColumnInfo(name = KEY_COMPRA_CALIBRE1) var calibre1: String,

        @ColumnInfo(name = KEY_COMPRA_CALIBRE2) var calibre2: String,

        @ColumnInfo(name = KEY_COMPRA_UNIDADES) var unidades: Int,

        @ColumnInfo(name = KEY_COMPRA_PRECIO) var precio: Double,

        @ColumnInfo(name = KEY_COMPRA_FECHA) var fecha: String,

        @ColumnInfo(name = KEY_COMPRA_TIPO) var tipo: String,

        @ColumnInfo(name = KEY_COMPRA_PESO) var peso: Int = 0,

        @ColumnInfo(name = KEY_COMPRA_MARCA) var marca: String,

        @ColumnInfo(name = KEY_COMPRA_TIENDA) var tienda: String,

        @ColumnInfo(name = KEY_COMPRA_VALORACION) var valoracion: Float,

        @ColumnInfo(name = KEY_COMPRA_IMAGEN) var imagePath: String
)
