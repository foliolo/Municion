package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_APODO
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_CALIBRE1
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_CALIBRE2
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_CUPO
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_GASTADO
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_ID_COMPRA
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_ID_LICENCIA
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_IMAGEN
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_MARCA
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_MODELO
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_NUM_ARMA
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_NUM_GUIA
import al.ahgitdevelopment.municion.repository.dao.KEY_GUIA_TIPO_ARMA
import al.ahgitdevelopment.municion.repository.dao.TABLE_GUIAS
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Alberto on 13/05/2016.
 */

@Entity(
    tableName = TABLE_GUIAS
    // foreignKeys = [
    //     ForeignKey(
    //         entity = Compra::class,
    //         parentColumns = [KEY_ID],
    //         childColumns = [KEY_GUIA_ID_COMPRA],
    //         onDelete = ForeignKey.CASCADE
    //     ),
    //     ForeignKey(
    //         entity = License::class,
    //         parentColumns = [KEY_ID],
    //         childColumns = [KEY_GUIA_ID_LICENCIA],
    //         onDelete = ForeignKey.CASCADE
    //     )
    // ]
)
open class Guia(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_GUIA_ID_COMPRA) var idCompra: Long,

    @ColumnInfo(name = KEY_GUIA_ID_LICENCIA) var tipoLicencia: Long,

    @ColumnInfo(name = KEY_GUIA_APODO) var apodo: String,

    @ColumnInfo(name = KEY_GUIA_MARCA) var marca: String,

    @ColumnInfo(name = KEY_GUIA_MODELO) var modelo: String,

    @ColumnInfo(name = KEY_GUIA_TIPO_ARMA) var tipoArma: Int,

    @ColumnInfo(name = KEY_GUIA_CALIBRE1) var calibre1: String,

    @ColumnInfo(name = KEY_GUIA_CALIBRE2) var calibre2: String,

    @ColumnInfo(name = KEY_GUIA_NUM_GUIA) var numGuia: String,

    @ColumnInfo(name = KEY_GUIA_NUM_ARMA) var numArma: String,

    @ColumnInfo(name = KEY_GUIA_IMAGEN) var imagePath: String,

    @ColumnInfo(name = KEY_GUIA_CUPO) var cupo: Int,

    @ColumnInfo(name = KEY_GUIA_GASTADO) var gastado: Int
) : Serializable {
    constructor(bundle: Bundle) : this(
        id = 0L,
        idCompra = 0L,
        tipoLicencia = 0L,
        apodo = "",
        marca = "",
        modelo = "",
        tipoArma = 0,
        calibre1 = "",
        calibre2 = "",
        numGuia = "",
        numArma = "",
        imagePath = "",
        cupo = 0,
        gastado = 0
    ) {
        idCompra = bundle.getLong(KEY_GUIA_ID_COMPRA)
        tipoLicencia = bundle.getLong(KEY_GUIA_ID_LICENCIA)
        apodo = bundle.getString(KEY_GUIA_APODO) ?: ""
        marca = bundle.getString(KEY_GUIA_MARCA) ?: ""
        modelo = bundle.getString(KEY_GUIA_MODELO) ?: ""
        tipoArma = bundle.getInt(KEY_GUIA_TIPO_ARMA)
        calibre1 = bundle.getString(KEY_GUIA_CALIBRE1) ?: ""
        calibre2 = bundle.getString(KEY_GUIA_CALIBRE2) ?: ""
        numGuia = bundle.getString(KEY_GUIA_NUM_GUIA) ?: ""
        numArma = bundle.getString(KEY_GUIA_NUM_ARMA) ?: ""
        imagePath = bundle.getString(KEY_GUIA_IMAGEN) ?: ""
        cupo = bundle.getInt(KEY_GUIA_CUPO)
        gastado = bundle.getInt(KEY_GUIA_GASTADO)
    }
}
