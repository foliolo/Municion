package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Compra
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CompraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg compra: Compra)

    @Update
    fun update(vararg compra: Compra)

    @Query("DELETE FROM $TABLE_COMPRAS")
    fun deleteAll()

    @Query("SELECT * from $TABLE_COMPRAS")
    fun retrieveCompras(): LiveData<List<Compra>>

    @Query("SELECT * from $TABLE_COMPRAS WHERE $KEY_ID = :compraId")
    fun getCompraById(compraId: Long): LiveData<Compra>
}