package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import androidx.lifecycle.LiveData

interface RepositoryInterface {
    suspend fun getProperties(): LiveData<List<Property>>?
    suspend fun getPurchases(): LiveData<List<Purchase>>?
    suspend fun getLicenses(): LiveData<List<License>>?
    suspend fun getTiradas(): LiveData<List<Competition>>?

    fun saveProperty(property: Property)
    fun savePurchase(purchase: Purchase)
    suspend fun saveLicense(license: License)
    fun saveTiradas(competition: Competition)

    suspend fun removeProperties(id: Long)
    suspend fun removePurchase(id: Long)
    suspend fun removeLicense(id: Long)
    suspend fun removeTirada(id: Long)

    fun fetchDataFromFirebase()
    fun uploadDataToFirebase()
}
