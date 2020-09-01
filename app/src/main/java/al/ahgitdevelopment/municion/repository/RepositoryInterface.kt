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
    suspend fun getCompetition(): LiveData<List<Competition>>?

    suspend fun saveProperty(property: Property)
    suspend fun savePurchase(purchase: Purchase)
    suspend fun saveLicense(license: License)
    suspend fun saveCompetition(competition: Competition)

    suspend fun removeProperty(id: Long)
    suspend fun removePurchase(id: Long)
    suspend fun removeLicense(id: Long)
    suspend fun removeCompetition(id: Long)

    fun fetchDataFromFirebase()
    fun uploadDataToFirebase()
}
