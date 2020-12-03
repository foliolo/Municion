package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DATE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DESCRIPTION
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_PLACE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_POINTS
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_RANKING
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_COMPETITION
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Created by ahidalgog on 12/01/2017.
 */
@Entity(tableName = TABLE_COMPETITION)
data class Competition(

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = KEY_COMPETITION_DESCRIPTION) var description: String,

    @ColumnInfo(name = KEY_COMPETITION_DATE) var date: String,

    @ColumnInfo(name = KEY_COMPETITION_RANKING) var ranking: String,

    @ColumnInfo(name = KEY_COMPETITION_POINTS) var points: Int,

    @ColumnInfo(name = KEY_COMPETITION_PLACE) var place: String
) {
    constructor() : this(
        id = "",
        description = "",
        date = "",
        ranking = "",
        points = 0,
        place = "",
    )
}
