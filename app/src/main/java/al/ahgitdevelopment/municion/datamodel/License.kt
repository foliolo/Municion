package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.KEY_ID
import al.ahgitdevelopment.municion.repository.dao.KEY_LICENCIAS_FECHA_CADUCIDAD
import al.ahgitdevelopment.municion.repository.dao.KEY_LICENCIAS_FECHA_EXPEDICION
import al.ahgitdevelopment.municion.repository.dao.KEY_LICENCIAS_NOMBRE
import al.ahgitdevelopment.municion.repository.dao.KEY_LICENCIAS_NUM_LICENCIA
import al.ahgitdevelopment.municion.repository.dao.KEY_LICENCIAS_NUM_SEGURO
import al.ahgitdevelopment.municion.repository.dao.TABLE_LICENCIAS
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Alberto on 13/05/2016.
 */
@Entity(tableName = TABLE_LICENCIAS)
data class License(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_LICENCIAS_NOMBRE) var licenseName: String,

    @ColumnInfo(name = KEY_LICENCIAS_NUM_LICENCIA) var licenseNumber: String,

    @ColumnInfo(name = KEY_LICENCIAS_FECHA_EXPEDICION) var issueDate: String,

    @ColumnInfo(name = KEY_LICENCIAS_FECHA_CADUCIDAD) var expiryDate: String,

    @ColumnInfo(name = KEY_LICENCIAS_NUM_SEGURO) var insuranceNumber: String

) : Serializable
