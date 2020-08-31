package al.ahgitdevelopment.municion.repository

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import androidx.lifecycle.LiveData
import javax.inject.Inject

class Repository @Inject constructor(private val db: AppDatabase) : RepositoryInterface {

    override suspend fun getProperties(): LiveData<List<Property>>? = db.propertyDao()?.getProperties()
    override suspend fun getPurchases(): LiveData<List<Purchase>>? = db.purchaseDao()?.getPurchases()
    override suspend fun getLicenses(): LiveData<List<License>>? = db.licenseDao()?.getLicenses()
    override suspend fun getTiradas(): LiveData<List<Competition>>? = db.tiradaDao()?.getCompetitions()

    override fun saveProperty(property: Property) = db.propertyDao()?.insert(property)!!
    override fun savePurchase(purchase: Purchase) = db.purchaseDao()?.insert(purchase)!!
    override suspend fun saveLicense(license: License) = db.licenseDao()?.insert(license)!!
    override fun saveTiradas(competition: Competition) = db.tiradaDao()?.insert(competition)!!

    override suspend fun removeProperties(id: Long) = db.propertyDao()?.delete(id)!!
    override suspend fun removePurchase(id: Long) = db.purchaseDao()?.delete(id)!!
    override suspend fun removeLicense(id: Long) = db.licenseDao()?.delete(id)!!
    override suspend fun removeTirada(id: Long) = db.tiradaDao()?.delete(id)!!

    override fun fetchDataFromFirebase() {
        TODO("Not yet implemented")
    }

    override fun uploadDataToFirebase() {
        TODO("Not yet implemented")
    }
}
