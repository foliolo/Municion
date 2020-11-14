package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_DATE_EXPIRY
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_DATE_ISSUE
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_INSURANCE_NUMBER
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_NAME
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_NUMBER
import al.ahgitdevelopment.municion.repository.database.TABLE_LICENSES
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Alberto on 13/05/2016.
 */
@Entity(tableName = TABLE_LICENSES)
data class License(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_LICENSE_NAME) var licenseName: String,

    @ColumnInfo(name = KEY_LICENSE_NUMBER) var licenseNumber: String,

    @ColumnInfo(name = KEY_LICENSE_DATE_ISSUE) var issueDate: String,

    @ColumnInfo(name = KEY_LICENSE_DATE_EXPIRY) var expiryDate: String,

    @ColumnInfo(name = KEY_LICENSE_INSURANCE_NUMBER) var insuranceNumber: String

) : Serializable
