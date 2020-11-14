package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.database.AppDatabase
import al.ahgitdevelopment.municion.repository.database.DATABASE_NAME
import al.ahgitdevelopment.municion.repository.database.dao.CompetitionDao
import al.ahgitdevelopment.municion.repository.database.dao.LicenseDao
import al.ahgitdevelopment.municion.repository.database.dao.PropertyDao
import al.ahgitdevelopment.municion.repository.database.dao.PurchaseDao
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

private lateinit var INSTANCE: AppDatabase

@Module
@InstallIn(ApplicationComponent::class)
class DatabaseModule {

    @Provides
    fun provideDataBase(@ApplicationContext appContext: Context): AppDatabase {
        synchronized(AppDatabase::class) {
            if (!::INSTANCE.isInitialized) {
                INSTANCE = Room
                    .databaseBuilder(
                        appContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                    // .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }
        return INSTANCE
    }

    @Provides
    fun providesLicenseDao(): LicenseDao = INSTANCE.licenseDao()

    @Provides
    fun providesPropertyDao(): PropertyDao = INSTANCE.propertyDao()

    @Provides
    fun providesPurchaseDao(): PurchaseDao = INSTANCE.purchaseDao()

    @Provides
    fun providesCompetitionDao(): CompetitionDao = INSTANCE.competitionDao()

    companion object {

        // EXAMPLE, NOT NEEDED YET
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE new_Song (
                        id INTEGER PRIMARY KEY NOT NULL,
                        name TEXT,
                        tag TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT INTO new_Song (id, name, tag)
                    SELECT id, name, tag FROM Song
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE Song")
                database.execSQL("ALTER TABLE new_Song RENAME TO Song")
            }
        }
    }
}
