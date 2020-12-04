package al.ahgitdevelopment.municion.repository.database

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.DataSourceContract
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(private val db: AppDatabase) : DataSourceContract {
    override var properties: Flow<List<Property>> = db.propertyDao().getProperties()
    override var purchases: Flow<List<Purchase>> = db.purchaseDao().getPurchases()
    override var licenses: Flow<List<License>> = db.licenseDao().getLicenses()
    override var competitions: Flow<List<Competition>> = db.competitionDao().getCompetitions()

    override suspend fun saveProperty(property: Property) = db.propertyDao().insert(property)
    override suspend fun savePurchase(purchase: Purchase) = db.purchaseDao().insert(purchase)

    @WorkerThread
    override suspend fun saveLicense(license: License) = db.licenseDao().insert(license)
    override suspend fun saveCompetition(competition: Competition) = db.competitionDao().insert(competition)

    override suspend fun removeProperty(id: String) = db.propertyDao().delete(id)
    override suspend fun removePurchase(id: String) = db.purchaseDao().delete(id)

    @WorkerThread
    override suspend fun removeLicense(id: String) = db.licenseDao().delete(id)
    override suspend fun removeCompetition(id: String) = db.competitionDao().delete(id)

    override suspend fun removeAllLicenses() = db.licenseDao().deleteAll()
    override suspend fun removeAllProperties() = db.propertyDao().deleteAll()
    override suspend fun removeAllPurchases() = db.purchaseDao().deleteAll()
    override suspend fun removeAllCompetitions() = db.competitionDao().deleteAll()
}
