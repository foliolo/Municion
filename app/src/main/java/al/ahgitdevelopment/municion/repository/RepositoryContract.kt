package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import kotlinx.coroutines.flow.Flow

interface RepositoryContract {
    // var properties: Flow<List<Property>>
    // var purchases: Flow<List<Purchase>>
    // var licenses: Flow<List<License>>
    // var competitions: Flow<List<Competition>>

    fun getProperties(): Flow<List<Property>>
    fun getPurchases(): Flow<List<Purchase>>
    fun getLicenses(forceUpdate: Boolean): Flow<List<License>>
    fun getCompetitions(): Flow<List<Competition>>

    suspend fun saveProperty(property: Property)
    suspend fun savePurchase(purchase: Purchase)
    suspend fun saveLicense(license: License)
    suspend fun saveCompetition(competition: Competition)

    suspend fun removeProperty(id: String)
    suspend fun removePurchase(id: String)
    suspend fun removeLicense(id: String)
    suspend fun removeCompetition(id: String)
}
