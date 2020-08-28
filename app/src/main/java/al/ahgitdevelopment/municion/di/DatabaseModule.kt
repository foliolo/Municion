package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import al.ahgitdevelopment.municion.repository.dao.DATABASE_NAME
import al.ahgitdevelopment.municion.repository.dao.LicenseDao
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule(val context: Context) {

    private val database = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun providesRoomDatabase(): AppDatabase = database

    @Provides
    fun providesLicenseDao(): LicenseDao = database.licenciaDao()!!
}
