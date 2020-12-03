package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BORE1
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BORE2
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BRAND
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_IMAGE
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_MODEL
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_NICKNAME
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_NUM_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_PROPERTIES
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Created by Alberto on 13/05/2016.
 */

@Entity(tableName = TABLE_PROPERTIES)
data class Property(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = KEY_PROPERTY_NICKNAME) var nickname: String,

    @ColumnInfo(name = KEY_PROPERTY_BRAND) var brand: String,

    @ColumnInfo(name = KEY_PROPERTY_MODEL) var model: String,

    @ColumnInfo(name = KEY_PROPERTY_BORE1) var bore1: String,

    @ColumnInfo(name = KEY_PROPERTY_BORE2) var bore2: String,

    @ColumnInfo(name = KEY_PROPERTY_NUM_ID) var numId: String,

    @ColumnInfo(name = KEY_PROPERTY_IMAGE) var image: String

) {
    constructor() : this(
        id = "",
        nickname = "",
        brand = "",
        model = "",
        bore1 = "",
        bore2 = "",
        numId = "",
        image = ""
    )
}
