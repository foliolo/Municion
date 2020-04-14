package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.*
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Alberto on 13/05/2016.
 */
@Entity(tableName = TABLE_LICENCIAS)
data class Licencia(

        @PrimaryKey(autoGenerate = true)
        @NonNull
        @ColumnInfo(name = KEY_ID) var id: Long,

        @ColumnInfo(name = KEY_LICENCIAS_TIPO) var tipo: Int,

        @ColumnInfo(name = KEY_LICENCIAS_NOMBRE) var nombre: String,

        @ColumnInfo(name = KEY_LICENCIAS_TIPO_PERMISO_CONDUCCION) var tipoPermisoConduccion: Int,

        @ColumnInfo(name = KEY_LICENCIAS_EDAD) var edad: Int,

        @ColumnInfo(name = KEY_LICENCIAS_FECHA_EXPEDICION) var fechaExpedicion: String,

        @ColumnInfo(name = KEY_LICENCIAS_FECHA_CADUCIDAD) var fechaCaducidad: String,

        @ColumnInfo(name = KEY_LICENCIAS_NUM_LICENCIA) var numLicencia: String,

        @ColumnInfo(name = KEY_LICENCIAS_NUM_ABONADO) var numAbonado: Int,

        @ColumnInfo(name = KEY_LICENCIAS_NUM_SEGURO) var numSeguro: String,

        @ColumnInfo(name = KEY_LICENCIAS_AUTONOMIA) var autonomia: Int,

        @ColumnInfo(name = KEY_LICENCIAS_ESCALA) var escala: Int,

        @ColumnInfo(name = KEY_LICENCIAS_CATEGORIA) var categoria: Int

) : Serializable
