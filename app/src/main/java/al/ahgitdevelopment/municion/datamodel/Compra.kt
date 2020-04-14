package al.ahgitdevelopment.municion.datamodel

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Alberto on 12/05/2016.
 */
open class Compra(
        var id: Int = 0,
        var idPosGuia: Int = 0,
        var calibre1: String? = null,
        var calibre2: String? = null,
        var unidades: Int = 0,
        var precio: Double = 0.toDouble(),
        var fecha: String? = null,
        var tipo: String? = null,
        var peso: Int = 0,
        var marca: String? = null,
        var tienda: String? = null,
        var valoracion: Float = 0f,
        var imagePath: String? = null
) : Parcelable {

    protected constructor(input: Parcel) : this() {
        id = input.readInt()
        idPosGuia = input.readInt()
        calibre1 = input.readString()
        calibre2 = input.readString()
        unidades = input.readInt()
        precio = input.readDouble()
        fecha = input.readString()
        tipo = input.readString()
        peso = input.readInt()
        marca = input.readString()
        tienda = input.readString()
        valoracion = input.readFloat()
        imagePath = input.readString()
    }

    constructor(extras: Bundle) : this() {
        idPosGuia = extras.getInt("idPosGuia")
        calibre1 = extras.getString("calibre1")
        calibre2 = extras.getString("calibre2", "")
        unidades = extras.getInt("unidades")
        precio = extras.getDouble("precio")
        fecha = extras.getString("fecha", "")
        tipo = extras.getString("tipo")
        peso = extras.getInt("peso")
        marca = extras.getString("marca")
        tienda = extras.getString("tienda")
        valoracion = extras.getFloat("valoracion")
        imagePath = extras.getString("imagePath")
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(idPosGuia)
        dest.writeString(calibre1)
        dest.writeString(calibre2)
        dest.writeInt(unidades)
        dest.writeDouble(precio)
        dest.writeString(fecha)
        dest.writeString(tipo)
        dest.writeInt(peso)
        dest.writeString(marca)
        dest.writeString(tienda)
        dest.writeFloat(valoracion)
        dest.writeString(imagePath)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Compra> = object : Parcelable.Creator<Compra> {
            override fun createFromParcel(input: Parcel): Compra? {
                return Compra(input)
            }

            override fun newArray(size: Int): Array<Compra?> {
                return arrayOfNulls(size)
            }
        }
    }
}
