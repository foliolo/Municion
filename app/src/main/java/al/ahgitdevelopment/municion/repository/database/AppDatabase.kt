package al.ahgitdevelopment.municion.repository.database

import al.ahgitdevelopment.municion.datamodel.Competition
import al.ahgitdevelopment.municion.datamodel.License
import al.ahgitdevelopment.municion.datamodel.Property
import al.ahgitdevelopment.municion.datamodel.Purchase
import al.ahgitdevelopment.municion.repository.database.dao.CompetitionDao
import al.ahgitdevelopment.municion.repository.database.dao.LicenseDao
import al.ahgitdevelopment.municion.repository.database.dao.PropertyDao
import al.ahgitdevelopment.municion.repository.database.dao.PurchaseDao
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Property::class,
        Purchase::class,
        License::class,
        Competition::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
    abstract fun propertyDao(): PropertyDao
    abstract fun licenseDao(): LicenseDao
    abstract fun competitionDao(): CompetitionDao
}
