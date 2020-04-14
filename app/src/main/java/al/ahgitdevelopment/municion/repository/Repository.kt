package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.Licencia
import al.ahgitdevelopment.municion.datamodel.Tirada
import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import al.ahgitdevelopment.municion.repository.dao.DATABASE_NAME
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import javax.inject.Inject

class Repository @Inject constructor(var context: Context, var db: AppDatabase) : RepositoryInterface {

    init {
        db = Room.databaseBuilder(context,
                AppDatabase::class.java,
                DATABASE_NAME).build()
    }

    override fun getGuias(): LiveData<List<Guia>>? {
        return db.guiaDao()?.retrieveGuias()
    }

    override fun getCompras(): LiveData<List<Compra>>? {
        return db.compraDao()?.retrieveCompras()
    }

    override fun getLicencias(): LiveData<List<Licencia>>? {
        return db.licenciaDao()?.retrieveLicencias()
    }

    override fun getTiradas(): LiveData<List<Tirada>>? {
        return db.tiradaDao()?.retrieveTiradas()
    }

    override fun saveGuias(guia: Guia) {
        db.guiaDao()?.insert(guia)
    }

    override fun saveCompras(compra: Compra) {
        db.compraDao()?.insert(compra)
    }

    override fun saveLicencias(licencia: Licencia) {
        db.licenciaDao()?.insert(licencia)
    }

    override fun saveTiradas(tirada: Tirada) {
        db.tiradaDao()?.insert(tirada)
    }

    override fun fetchDataFromFirebase() {
        TODO("Not yet implemented")
    }

    override fun uploadDataToFirebase() {
        TODO("Not yet implemented")
    }

}
