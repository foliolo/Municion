package al.ahgitdevelopment.municion.repository.firebase

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.DataSourceContract
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemoteDataSource @Inject internal constructor(
    firebaseDb: FirebaseDatabase
) : DataSourceContract {
    override var properties: Flow<List<Property>>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var purchases: Flow<List<Purchase>>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var licenses: Flow<List<License>>
        get() = TODO("Not yet implemented")
        set(value) {}
    override var competitions: Flow<List<Competition>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun saveProperty(property: Property) {
        TODO("Not yet implemented")
    }

    override suspend fun savePurchase(purchase: Purchase) {
        TODO("Not yet implemented")
    }

    override suspend fun saveLicense(license: License) {
        TODO("Not yet implemented")
    }

    override suspend fun saveCompetition(competition: Competition) {
        TODO("Not yet implemented")
    }

    override suspend fun removeProperty(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removePurchase(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removeLicense(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removeCompetition(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun removeAllLicenses() {
        TODO("Not yet implemented")
    }
    // override var properties: Flow<List<Property>> = db.propertyDao().getProperties()
    // override var purchases: Flow<List<Purchase>> = db.purchaseDao().getPurchases()
    // override var licenses: Flow<List<License>> = db.licenseDao().getLicenses()
    // override var competitions: Flow<List<Competition>> = db.competitionDao().getCompetitions()
    //
    // override suspend fun saveProperty(property: Property) = db.propertyDao().insert(property)
    // override suspend fun savePurchase(purchase: Purchase) = db.purchaseDao().insert(purchase)
    //
    // @WorkerThread
    // override suspend fun saveLicense(license: License) = db.licenseDao().insert(license)
    // override suspend fun saveCompetition(competition: Competition) = db.competitionDao().insert(competition)
    //
    // override suspend fun removeProperty(id: Long) = db.propertyDao().delete(id)
    // override suspend fun removePurchase(id: Long) = db.purchaseDao().delete(id)
    //
    // @WorkerThread
    // override suspend fun removeLicense(id: Long) = db.licenseDao().delete(id)
    // override suspend fun removeCompetition(id: Long) = db.competitionDao().delete(id)
}
