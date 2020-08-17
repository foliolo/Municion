package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.*
import al.ahgitdevelopment.municion.sandbox.Utils
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

/**
 * Created by ahidalgog on 12/01/2017.
 */
@Entity(tableName = TABLE_TIRADAS)
data class Tirada(

        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = KEY_ID) var id: Long,

        @ColumnInfo(name = KEY_TIRADAS_DESCRIPCION) var descripcion: String,

        @ColumnInfo(name = KEY_TIRADAS_RANGO) var rango: String,

        @ColumnInfo(name = KEY_TIRADAS_FECHA) var fecha: String,

        @ColumnInfo(name = KEY_TIRADAS_PUNTUACION) var puntuacion: Int

) : Serializable {

    constructor(bundle: Bundle) : this(
            id = 0L,
            descripcion = "",
            rango = "",
            fecha = "",
            puntuacion = 0
    ) {
        id = bundle.getLong(KEY_ID)
        descripcion = bundle.getString(KEY_TIRADAS_DESCRIPCION) ?: ""
        rango = bundle.getString(KEY_TIRADAS_RANGO) ?: ""
        fecha = bundle.getString(KEY_TIRADAS_FECHA) ?: ""
        puntuacion = bundle.getInt(KEY_TIRADAS_PUNTUACION)
    }

    /**
     * Método que obtiene los milisegundos que quedan desde la fecha actual hasta un años despues de la ultima tirada
     *
     * @param tirada Array de tiaradas realizadas
     * @return Milisegundos hasta que expire la licencia
     */
    fun millisUntilExpiracy(tirada: Tirada): Long {
        val ultimaTirada = Calendar.getInstance()
        ultimaTirada.time = Utils.getDateFromString(tirada.fecha)
        val expiracy = Calendar.getInstance()
        expiracy.time = ultimaTirada.time
        expiracy[Calendar.YEAR] = expiracy[Calendar.YEAR] + 1
        return expiracy.timeInMillis - Calendar.getInstance().timeInMillis
    }
}
