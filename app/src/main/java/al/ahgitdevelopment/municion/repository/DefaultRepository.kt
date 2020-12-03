package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        return wrapEspressoIdlingResource {

            // Retrieve data locally and from firebase
            // val remoteLicenses = remoteDataSource.licenses
            // val localLicenses = localDataSource.licenses

            if (forceUpdate) {
                remoteDataSource.licenses.map { licenses ->
                    localDataSource.removeAllLicenses()
                    licenses.forEach { localDataSource.saveLicense(it) }
                    licenses
                }
            } else {
                localDataSource.licenses
            }

            /*
            remoteLicenses.combineTransform(localLicenses) { remote, local ->

                remote.forEach { remoteLicense ->
                    // If license.id already exist in local database, then update the element
                    local.find { it.id == remoteLicense.id }?.let { localLicense ->
                        localDataSource.removeLicense(localLicense!!.id)
                        localDataSource.saveLicense(remoteLicense)
                        local.toMutableList().remove(localLicense)
                        local.toMutableList().add(remoteLicense)
                    }

                    // Otherwise, add the new element
                    if (!local.contains(remoteLicense)) {
                        localDataSource.saveLicense(remoteLicense)
                        local.toMutableList().add(remoteLicense)
                    }
                }

                emit(local)
            }
            */
        }
    }
    //
    // private suspend fun addLicense(remote: List<License>, local: List<License>) {
    //     remote.forEach { remoteLicense ->
    //         !local.contains(remoteLicense).apply {
    //             localDataSource.saveLicense(remoteLicense)
    //         }
    //     }
    // }
    //
    // private suspend fun updateLicense(remote: List<License>, local: List<License>) {
    //     remote.forEach { remoteLicense ->
    //         !local.contains(remoteLicense).apply {
    //             localDataSource.saveLicense(remoteLicense)
    //         }
    //     }
    // }

    override fun getCompetitions(): Flow<List<Competition>> {
        return wrapEspressoIdlingResource { localDataSource.competitions }
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
}
