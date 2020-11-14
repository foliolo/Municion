package al.ahgitdevelopment.municion.datamodel

import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DATE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DESCRIPTION
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_PLACE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_POINTS
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_RANKING
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_COMPETITION
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Created by ahidalgog on 12/01/2017.
 */
@Entity(tableName = TABLE_COMPETITION)
data class Competition(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = KEY_ID) var id: Long,

    @ColumnInfo(name = KEY_COMPETITION_DESCRIPTION) var description: String,

    @ColumnInfo(name = KEY_COMPETITION_DATE) var date: String,

    @ColumnInfo(name = KEY_COMPETITION_RANKING) var ranking: String,

    @ColumnInfo(name = KEY_COMPETITION_POINTS) var points: Int,

    @ColumnInfo(name = KEY_COMPETITION_PLACE) var place: String

) : Serializable, Comparable<Competition> {

    constructor(bundle: Bundle) : this(
        id = 0L,
        description = "",
        date = "",
        ranking = "",
        points = 0,
        place = "",
    ) {
        id = bundle.getLong(KEY_ID)
        description = bundle.getString(KEY_COMPETITION_DESCRIPTION) ?: ""
        date = bundle.getString(KEY_COMPETITION_DATE) ?: ""
        ranking = bundle.getString(KEY_COMPETITION_RANKING) ?: ""
        points = bundle.getInt(KEY_COMPETITION_POINTS)
        place = bundle.getString(KEY_COMPETITION_PLACE) ?: ""
    }

    override fun compareTo(other: Competition): Int = when {
        this.id < other.id -> -1
        this.id > other.id -> 1
        else -> 0
    }

    companion object {
        private val COMPARATOR = Comparator<Competition> { o1, o2 ->
            when {
                o1.description < o2.description -> -1
                o1.description > o2.description -> 1
                else -> 0
            }
        }.thenComparator { o1, o2 ->
            when {
                o1.id < o2.id -> -1
                o1.id > o2.id -> 1
                else -> 0
            }
        }
    }
}
