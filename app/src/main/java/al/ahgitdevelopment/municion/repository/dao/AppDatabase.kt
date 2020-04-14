package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Compra
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        entities = [
            Compra::class
        ],
        version = DATABASE_VERSION,
        exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun compraDao(): CompraDao?
    abstract fun guiaDao(): GuiaDao?
    abstract fun licenciaDao(): LicenciaDao?
    abstract fun tiradaDao(): TiradaDao?
}