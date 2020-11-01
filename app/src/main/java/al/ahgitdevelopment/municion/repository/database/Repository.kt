package al.ahgitdevelopment.municion.repository.database

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.database.dao.AppDatabase
import androidx.lifecycle.LiveData
import javax.inject.Inject

open class Repository @Inject constructor(private val db: AppDatabase) : RepositoryInterface {

    override suspend fun getProperties(): LiveData<List<Property>>? = db.propertyDao()?.getProperties()
    override suspend fun getPurchases(): LiveData<List<Purchase>>? = db.purchaseDao()?.getPurchases()
    override suspend fun getLicenses(): LiveData<List<License>>? = db.licenseDao()?.getLicenses()
    override suspend fun getCompetition(): LiveData<List<Competition>>? =
        db.competitionDao()?.getCompetitions()

    override suspend fun saveProperty(property: Property) = db.propertyDao()?.insert(property)!!
    override suspend fun savePurchase(purchase: Purchase) = db.purchaseDao()?.insert(purchase)!!
    override suspend fun saveLicense(license: License) = db.licenseDao()?.insert(license)!!
    override suspend fun saveCompetition(competition: Competition) =
        db.competitionDao()?.insert(competition)!!

    override suspend fun removeProperty(id: Long) = db.propertyDao()?.delete(id)!!
    override suspend fun removePurchase(id: Long) = db.purchaseDao()?.delete(id)!!
    override suspend fun removeLicense(id: Long) = db.licenseDao()?.delete(id)!!
    override suspend fun removeCompetition(id: Long) = db.competitionDao()?.delete(id)!!

    override fun fetchDataFromFirebase() {
        TODO("Not yet implemented")
    }

    override fun uploadDataToFirebase() {
        TODO("Not yet implemented")
    }
}
