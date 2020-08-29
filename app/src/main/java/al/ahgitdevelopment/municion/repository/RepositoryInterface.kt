package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.lifecycle.LiveData

interface RepositoryInterface {
    // suspend fun getGuias(): LiveData<List<Guia>>?
    // suspend fun getCompras(): LiveData<List<Compra>>?
    suspend fun getLicenses(): LiveData<List<License>>?
    // suspend fun getTiradas(): LiveData<List<Tirada>>?

    fun saveGuias(guia: Guia)
    fun saveCompras(compra: Compra)
    suspend fun saveLicense(license: License)
    fun saveTiradas(tirada: Tirada)

    fun removeGuias(id: Long)
    fun removeCompra(id: Long)
    suspend fun removeLicense(id: Long)
    fun removeTirada(id: Long)

    fun fetchDataFromFirebase()
    fun uploadDataToFirebase()
}
