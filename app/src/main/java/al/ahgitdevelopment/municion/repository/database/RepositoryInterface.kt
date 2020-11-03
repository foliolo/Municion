package al.ahgitdevelopment.municion.repository.database

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import kotlinx.coroutines.flow.Flow

interface RepositoryInterface {
    suspend fun getProperties(): List<Property>?
    suspend fun getPurchases(): List<Purchase>?
    suspend fun getLicenses(): Flow<List<License>>
    suspend fun getCompetitions(): Flow<List<Competition>>

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
