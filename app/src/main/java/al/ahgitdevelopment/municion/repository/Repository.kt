package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Tirada
import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import androidx.lifecycle.LiveData
import javax.inject.Inject

class Repository @Inject constructor(private val db: AppDatabase) : RepositoryInterface {

    // override suspend fun getGuias(): LiveData<List<Guia>>? {
    //     return db.guiaDao()?.retrieveGuias()
    // }
    //
    // override suspend fun getCompras(): LiveData<List<Compra>>? {
    //     return db.compraDao()?.retrieveCompras()
    // }

    override suspend fun getLicenses(): LiveData<List<License>>? {
        return db.licenciaDao()?.getLicenses()
    }

    // override suspend fun getTiradas(): LiveData<List<Tirada>>? {
    //     return db.tiradaDao()?.retrieveTiradas()
    // }

    override fun saveGuias(guia: Guia) {
        db.guiaDao()?.insert(guia)
    }

    override fun saveCompras(compra: Compra) {
        db.compraDao()?.insert(compra)
    }

    override suspend fun saveLicense(license: License) {
        db.licenciaDao()?.insert(license)
    }

    override fun saveTiradas(tirada: Tirada) {
        db.tiradaDao()?.insert(tirada)
    }

    override fun removeGuias(id: Long) = db.guiaDao()?.delete(id)!!
    override fun removeCompra(id: Long) = db.compraDao()?.delete(id)!!
    override suspend fun removeLicense(id: Long) = db.licenciaDao()?.delete(id)!!
    override fun removeTirada(id: Long) = db.tiradaDao()?.delete(id)!!

    override fun fetchDataFromFirebase() {
        TODO("Not yet implemented")
    }

    override fun uploadDataToFirebase() {
        TODO("Not yet implemented")
    }
}
