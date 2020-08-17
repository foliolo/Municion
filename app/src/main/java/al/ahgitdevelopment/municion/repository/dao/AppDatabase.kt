package al.ahgitdevelopment.municion.repository.dao

import al.ahgitdevelopment.municion.datamodel.Compra
import al.ahgitdevelopment.municion.datamodel.Guia
import al.ahgitdevelopment.municion.datamodel.Licencia
import al.ahgitdevelopment.municion.datamodel.Tirada
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
        entities = [
            Guia::class,
            Compra::class,
            Licencia::class,
            Tirada::class
        ],
        version = DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun compraDao(): CompraDao?
    abstract fun guiaDao(): GuiaDao?
    abstract fun licenciaDao(): LicenciaDao?
    abstract fun tiradaDao(): TiradaDao?
}