package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.dao.KEY_ID
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_BORE1
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_BORE2
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_BRAND
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_IMAGE
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_MODEL
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_NICKNAME
import al.ahgitdevelopment.municion.repository.dao.KEY_PROPERTY_NUM_ID
import al.ahgitdevelopment.municion.repository.dao.TABLE_PROPERTIES
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
    tableName = TABLE_PROPERTIES
)
open class Property(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_PROPERTY_NICKNAME) var nickname: String,

    @ColumnInfo(name = KEY_PROPERTY_BRAND) var brand: String,

    @ColumnInfo(name = KEY_PROPERTY_MODEL) var model: String,

    @ColumnInfo(name = KEY_PROPERTY_BORE1) var bore1: String,

    @ColumnInfo(name = KEY_PROPERTY_BORE2) var bore2: String,

    @ColumnInfo(name = KEY_PROPERTY_NUM_ID) var numId: String,

    @ColumnInfo(name = KEY_PROPERTY_IMAGE) var image: String = ""

) : Serializable {

    constructor(bundle: Bundle) : this(
        id = 0L,
        nickname = "",
        brand = "",
        model = "",
        bore1 = "",
        bore2 = "",
        numId = "",
        image = "",
    ) {
        nickname = bundle.getString(KEY_PROPERTY_NICKNAME) ?: ""
        brand = bundle.getString(KEY_PROPERTY_BRAND) ?: ""
        model = bundle.getString(KEY_PROPERTY_MODEL) ?: ""
        bore1 = bundle.getString(KEY_PROPERTY_BORE1) ?: ""
        bore2 = bundle.getString(KEY_PROPERTY_BORE2) ?: ""
        numId = bundle.getString(KEY_PROPERTY_NUM_ID) ?: ""
        image = bundle.getString(KEY_PROPERTY_IMAGE) ?: ""
    }
}
