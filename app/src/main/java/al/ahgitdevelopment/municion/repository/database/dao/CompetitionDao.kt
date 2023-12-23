package al.ahgitdevelopment.municion.repository.database.dao

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.TABLE_COMPETITION
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg competition: Competition)

    @Update
    suspend fun update(vararg competition: Competition)

    @Query("DELETE FROM $TABLE_COMPETITION")
    suspend fun deleteAll()

    @Query("DELETE FROM $TABLE_COMPETITION WHERE $KEY_ID = :id")
    suspend fun delete(id: String)

    @Query("SELECT * from $TABLE_COMPETITION")
    fun getCompetitions(): Flow<List<Competition>>

    @Query("SELECT * from $TABLE_COMPETITION WHERE $KEY_ID = :competitionId")
    fun getCompetitionById(competitionId: Long): Competition
}
