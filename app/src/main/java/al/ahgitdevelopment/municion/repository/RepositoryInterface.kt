package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.lifecycle.LiveData

interface RepositoryInterface {
    fun getGuias(): LiveData<List<Guia>>?
    fun getCompras(): LiveData<List<Compra>>?
    suspend fun getLicenses(): LiveData<List<License>>?
    fun getTiradas(): LiveData<List<Tirada>>?

    fun saveGuias(guia: Guia)
    fun saveCompras(compra: Compra)
    suspend fun saveLicense(license: License)
    fun saveTiradas(tirada: Tirada)

    fun fetchDataFromFirebase()
    fun uploadDataToFirebase()
}
