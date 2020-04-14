package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.R
import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Alberto on 13/05/2016.
 */
data class Licencia(
        var id: Int = 0,
        var tipo: Int = 0,
        var nombre: String? = null,
        var tipoPermisoConduccion: Int = 0,
        var edad: Int = 0,
        var fechaExpedicion: String? = null,
        var fechaCaducidad: String? = null,
        var numLicencia: String? = null,
        var numAbonado: Int = 0,
        var numSeguro: String? = null,
        var autonomia: Int = 0,
        var escala: Int = 0,
        var categoria: Int = 0
) : Parcelable {

    constructor(input: Parcel) : this() {
        id = input.readInt()
        tipo = input.readInt()
        nombre = input.readString()
        tipoPermisoConduccion = input.readInt()
        edad = input.readInt()
        fechaExpedicion = input.readString()
        fechaCaducidad = input.readString()
        numLicencia = input.readString()
        numAbonado = input.readInt()
        numSeguro = input.readString()
        autonomia = input.readInt()
        escala = input.readInt()
        categoria = input.readInt()
    }

    constructor(context: Context, extras: Bundle) : this() {
        tipo = extras.getInt("tipo")
        nombre = getNombre(context)
        tipoPermisoConduccion = extras.getInt("tipo_permiso_conduccion", -1)
        edad = extras.getInt("edad")
        numLicencia = extras.getString("num_licencia", "")
        fechaExpedicion = extras.getString("fecha_expedicion", "")
        fechaCaducidad = extras.getString("fecha_caducidad", "")
        numAbonado = extras.getInt("num_abonado", -1)
        numSeguro = extras.getString("num_seguro", "")
        autonomia = extras.getInt("autonomia", -1)
        escala = extras.getInt("escala", -1)
        categoria = extras.getInt("categoria", -1)
    }

    constructor(licencia: Licencia) : this() {
        id = licencia.id
        tipo = licencia.tipo
        nombre = licencia.nombre
        tipoPermisoConduccion = licencia.tipoPermisoConduccion
        edad = licencia.edad
        numLicencia = licencia.numLicencia
        fechaExpedicion = licencia.fechaExpedicion
        fechaCaducidad = licencia.fechaCaducidad
        numAbonado = licencia.numAbonado
        numSeguro = licencia.numSeguro
        autonomia = licencia.autonomia
        escala = licencia.escala
        categoria = licencia.categoria
    }

    private fun getNombre(context: Context): String {
        return context.resources.getTextArray(R.array.tipo_licencias)[tipo].toString()
    }

    fun getStringEscala(context: Context): String {
        return context.resources.getTextArray(R.array.tipo_escala)[escala].toString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeInt(tipo)
        dest.writeString(nombre)
        dest.writeInt(tipoPermisoConduccion)
        dest.writeInt(edad)
        dest.writeString(fechaExpedicion)
        dest.writeString(fechaCaducidad)
        dest.writeString(numLicencia)
        dest.writeInt(numAbonado)
        dest.writeString(numSeguro)
        dest.writeInt(autonomia)
        dest.writeInt(escala)
        dest.writeInt(categoria)
    }

    companion object {
        val CREATOR: Parcelable.Creator<Licencia?> = object : Parcelable.Creator<Licencia?> {
            override fun createFromParcel(input: Parcel): Licencia? {
                return Licencia(input)
            }

            override fun newArray(size: Int): Array<Licencia?> {
                return arrayOfNulls(size)
            }
        }
    }
}
