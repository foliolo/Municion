package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.database.dao.KEY_ID
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_BORE1
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_BRAND
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_DATE
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_IMAGE
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_PRICE
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_RATING
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_STORE
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_UNITS
import al.ahgitdevelopment.municion.repository.database.dao.KEY_PURCHASE_WEIGHT
import al.ahgitdevelopment.municion.repository.database.dao.TABLE_PURCHASES
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by Alberto on 12/05/2016.
 */
@Entity(tableName = TABLE_PURCHASES)
open class Purchase(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_PURCHASE_BRAND) var brand: String,

    @ColumnInfo(name = KEY_PURCHASE_STORE) var store: String,

    @ColumnInfo(name = KEY_PURCHASE_BORE1) var bore: String,

    @ColumnInfo(name = KEY_PURCHASE_UNITS) var units: Int,

    @ColumnInfo(name = KEY_PURCHASE_PRICE) var price: Double,

    @ColumnInfo(name = KEY_PURCHASE_DATE) var date: String,

    @ColumnInfo(name = KEY_PURCHASE_RATING) var rating: Float,

    @ColumnInfo(name = KEY_PURCHASE_WEIGHT) var weight: Int,

    @ColumnInfo(name = KEY_PURCHASE_IMAGE) var image: String

) : Serializable
