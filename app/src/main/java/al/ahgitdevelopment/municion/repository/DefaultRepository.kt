package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

open class DefaultRepository(
    private val localDataSource: DataSourceContract,
    private val remoteDataSource: DataSourceContract
) : RepositoryContract {

    override fun getProperties(): Flow<List<Property>> {
        return wrapEspressoIdlingResource { localDataSource.properties }
    }

    override fun getPurchases(): Flow<List<Purchase>> {
        return wrapEspressoIdlingResource { localDataSource.purchases }
    }

    override fun getLicenses(forceUpdate: Boolean): Flow<List<License>> {
        return wrapEspressoIdlingResource { localDataSource.licenses }
    }

    override fun getCompetitions(): Flow<List<Competition>> {
        return wrapEspressoIdlingResource { localDataSource.competitions }
    }

    override suspend fun saveProperty(property: Property) {
        localDataSource.saveProperty(property)
    }

    override suspend fun savePurchase(purchase: Purchase) {
        localDataSource.savePurchase(purchase)
    }

    override suspend fun saveLicense(license: License) {
        localDataSource.saveLicense(license)
    }

    override suspend fun saveCompetition(competition: Competition) {
        localDataSource.saveCompetition(competition)
    }

    override suspend fun removeProperty(id: Long) {
        localDataSource.removeProperty(id)
    }

    override suspend fun removePurchase(id: Long) {
        localDataSource.removePurchase(id)
    }

    override suspend fun removeLicense(id: Long) {
        localDataSource.removeLicense(id)
    }

    override suspend fun removeCompetition(id: Long) {
        localDataSource.removeCompetition(id)
    }

    open suspend fun updateLicensesFromRemoteDataSource() {

        val remoteLicenses = remoteDataSource.licenses

        // Real apps might want to do a proper sync, deleting, modifying or adding each task.
        localDataSource.removeAllLicenses()
        remoteLicenses
            .catch { Log.e(TAG, "Error getting licenses", it) }
            .collect {
                it.forEach { license ->
                    localDataSource.saveLicense(license)
                }
            }
    }

    companion object {
        private val TAG = DefaultRepository.javaClass.name
    }
}
