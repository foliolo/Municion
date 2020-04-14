package al.ahgitdevelopment.municion.datamodel

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Alberto on 13/05/2016.
 */
open class Guia(
        var id: Int = 0,
        var idCompra: Int = 0,
        var tipoLicencia: Int = 0,
        var marca: String? = null,
        var modelo: String? = null,
        var apodo: String? = null,
        var tipoArma: Int = 0,
        var calibre1: String? = null,
        var calibre2: String? = null,
        var numGuia: String? = null,
        var numArma: String? = null,
        var imagePath: String? = null,
        var cupo: Int = 0,
        var gastado: Int = 0
) : Parcelable {

    constructor(input: Parcel) : this() {
        id = input.readInt()
        idCompra = input.readInt()
        tipoLicencia = input.readInt()
        marca = input.readString()
        modelo = input.readString()
        apodo = input.readString()
        tipoArma = input.readInt()
        calibre1 = input.readString()
        calibre2 = input.readString()
        numGuia = input.readString()
        numArma = input.readString()
        imagePath = input.readString()
        cupo = input.readInt()
        gastado = input.readInt()
    }

    constructor(extras: Bundle) : this() {
        tipoLicencia = extras.getInt("tipoLicencia")
        marca = extras.getString("marca")
        modelo = extras.getString("modelo")
        apodo = extras.getString("apodo")
        tipoArma = extras.getInt("tipoArma")
        calibre1 = extras.getString("calibre1")
        calibre2 = extras.getString("calibre2")
        numGuia = extras.getString("numGuia")
        numArma = extras.getString("numArma")
        imagePath = extras.getString("imagePath")
        gastado = extras.getInt("gastado")
        cupo = extras.getInt("cupo")
        imagePath = extras.getString("imagePath")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(idCompra)
        dest.writeInt(tipoLicencia)
        dest.writeString(marca)
        dest.writeString(modelo)
        dest.writeString(apodo)
        dest.writeInt(tipoArma)
        dest.writeString(calibre1)
        dest.writeString(calibre2)
        dest.writeString(numGuia)
        dest.writeString(numArma)
        dest.writeString(imagePath)
        dest.writeInt(cupo)
        dest.writeInt(gastado)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Guia?> = object : Parcelable.Creator<Guia?> {
            override fun createFromParcel(input: Parcel): Guia? {
                return Guia(input)
            }

            override fun newArray(size: Int): Array<Guia?> {
                return arrayOfNulls(size)
            }
        }
    }
}
