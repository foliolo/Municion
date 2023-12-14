package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import android.content.Context
import android.net.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

open class DefaultRepository(
    private val context: Context,
    private val localDataSource: DataSourceContract,
    private val remoteDataSource: DataSourceContract
) : RepositoryContract {

    override fun getProperties(): Flow<List<Property>> {
        return if (isConnected()) {
            remoteDataSource.properties.map { properties ->
                localDataSource.removeAllProperties()
                properties.forEach { localDataSource.saveProperty(it) }
                properties
            }
        } else {
            localDataSource.properties
        }
    }

    override fun getPurchases(): Flow<List<Purchase>> {
        return if (isConnected()) {
            remoteDataSource.purchases.map { purchases ->
                localDataSource.removeAllPurchases()
                purchases.forEach { localDataSource.savePurchase(it) }
                purchases
            }
        } else {
            localDataSource.purchases
        }
    }

    override fun getLicenses(): Flow<List<License>> {
        return if (isConnected()) {
            remoteDataSource.licenses.map { licenses ->
                localDataSource.removeAllLicenses()
                licenses.forEach { localDataSource.saveLicense(it) }
                licenses
            }
        } else {
            localDataSource.licenses
        }
    }

    override fun getCompetitions(): Flow<List<Competition>> {
        return if (isConnected()) {
            remoteDataSource.competitions.map { competitions ->
                localDataSource.removeAllCompetitions()
                competitions.forEach { localDataSource.saveCompetition(it) }
                competitions
            }
        } else {
            localDataSource.competitions
        }
    }

    override suspend fun saveProperty(property: Property) {
        localDataSource.saveProperty(property)
        remoteDataSource.saveProperty(property)
    }

    override suspend fun savePurchase(purchase: Purchase) {
        localDataSource.savePurchase(purchase)
        remoteDataSource.savePurchase(purchase)
    }

    override suspend fun saveLicense(license: License) {
        localDataSource.saveLicense(license)
        remoteDataSource.saveLicense(license)
    }

    override suspend fun saveCompetition(competition: Competition) {
        localDataSource.saveCompetition(competition)
        remoteDataSource.saveCompetition(competition)
    }

    override suspend fun removeProperty(id: String) {
        localDataSource.removeProperty(id)
        remoteDataSource.removeProperty(id)
    }

    override suspend fun removePurchase(id: String) {
        localDataSource.removePurchase(id)
        remoteDataSource.removePurchase(id)
    }

    override suspend fun removeLicense(id: String) {
        localDataSource.removeLicense(id)
        remoteDataSource.removeLicense(id)
    }

    override suspend fun removeCompetition(id: String) {
        localDataSource.removeCompetition(id)
        remoteDataSource.removeCompetition(id)
    }

    override fun savePropertyImageItem(itemId: String, imageUrl: String) {
        remoteDataSource.savePropertyImageItem(itemId, imageUrl)
    }

    override fun savePurchaseImageItem(itemId: String, imageUrl: String) {
        remoteDataSource.savePurchaseImageItem(itemId, imageUrl)
    }

    private fun isConnected(): Boolean {

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected) {
            return true
        }

        return false
    }
}
