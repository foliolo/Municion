package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_CALIBRE1
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_CALIBRE2
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_FECHA
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_ID_POS_GUIA
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_IMAGEN
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_MARCA
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_PESO
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_PRECIO
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_TIENDA
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_TIPO
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_UNIDADES
import al.ahgitdevelopment.municion.repository.dao.KEY_COMPRA_VALORACION
import al.ahgitdevelopment.municion.repository.dao.KEY_ID
import al.ahgitdevelopment.municion.repository.dao.TABLE_COMPRAS
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

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
) : Serializable
