package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.Utils
import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by ahidalgog on 12/01/2017.
 */
data class Tirada(
        var descripcion: String? = "",
        var rango: String? = "",
        var fecha: String? = "",
        var puntuacion: Int = 0
) : Parcelable {

    constructor(input: Parcel) : this() {
        descripcion = input.readString()
        rango = input.readString()
        fecha = input.readString()
        puntuacion = input.readInt()
    }

    constructor(tirada: Tirada) : this() {
        descripcion = tirada.descripcion
        rango = tirada.rango
        fecha = tirada.fecha
        puntuacion = tirada.puntuacion
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(descripcion)
        dest.writeString(rango)
        dest.writeString(fecha)
        dest.writeInt(puntuacion)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Tirada?> = object : Parcelable.Creator<Tirada?> {
            override fun createFromParcel(input: Parcel): Tirada? {
                return Tirada(input)
            }

            override fun newArray(size: Int): Array<Tirada?> {
                return arrayOfNulls(size)
            }
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
}
