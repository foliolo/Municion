package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Competition
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CompetitionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg competition: Competition)

    @Update
    fun update(vararg competition: Competition)

    @Query("DELETE FROM $TABLE_COMPETITION")
    fun deleteAll()

    @Query("DELETE FROM $TABLE_COMPETITION WHERE $KEY_ID = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * from $TABLE_COMPETITION")
    fun getCompetitions(): LiveData<List<Competition>>

    @Query("SELECT * from $TABLE_COMPETITION WHERE $KEY_ID = :competitionId")
    fun getCompetitionById(competitionId: Long): LiveData<Competition>
}