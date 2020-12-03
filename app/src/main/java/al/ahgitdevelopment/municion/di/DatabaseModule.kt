package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.database.AppDatabase
import al.ahgitdevelopment.municion.repository.database.DATABASE_NAME
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DATE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_DESCRIPTION
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_PLACE
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_POINTS
import al.ahgitdevelopment.municion.repository.database.KEY_COMPETITION_RANKING
import al.ahgitdevelopment.municion.repository.database.KEY_ID
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_DATE_EXPIRY
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_DATE_ISSUE
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_INSURANCE_NUMBER
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_NAME
import al.ahgitdevelopment.municion.repository.database.KEY_LICENSE_NUMBER
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BORE1
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BORE2
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_BRAND
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_IMAGE
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_MODEL
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_NICKNAME
import al.ahgitdevelopment.municion.repository.database.KEY_PROPERTY_NUM_ID
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_BORE1
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_BRAND
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_DATE
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_IMAGE
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_PRICE
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_RATING
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_STORE
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_UNITS
import al.ahgitdevelopment.municion.repository.database.KEY_PURCHASE_WEIGHT
import al.ahgitdevelopment.municion.repository.database.TABLE_COMPETITION
import al.ahgitdevelopment.municion.repository.database.TABLE_LICENSES
import al.ahgitdevelopment.municion.repository.database.TABLE_PROPERTIES
import al.ahgitdevelopment.municion.repository.database.TABLE_PURCHASES
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
                    .addMigrations(MIGRATION_1_2)
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
                // database.execSQL(
                //     """
                //     CREATE TABLE new_Song (
                //         id INTEGER PRIMARY KEY NOT NULL,
                //         name TEXT,
                //         tag TEXT NOT NULL DEFAULT ''
                //     )
                //     """.trimIndent()
                // )
                // database.execSQL(
                //     """
                //     INSERT INTO new_Song (id, name, tag)
                //     SELECT id, name, tag FROM Song
                //     """.trimIndent()
                // )

                database.beginTransaction()

                database.execSQL("DROP TABLE $TABLE_LICENSES")
                database.execSQL(
                    """
                        CREATE TABLE $TABLE_LICENSES (
                            $KEY_ID TEXT NOT NULL PRIMARY KEY,
                            $KEY_LICENSE_NAME TEXT NOT NULL,
                            $KEY_LICENSE_NUMBER TEXT NOT NULL,
                            $KEY_LICENSE_DATE_ISSUE TEXT NOT NULL,
                            $KEY_LICENSE_DATE_EXPIRY TEXT NOT NULL,
                            $KEY_LICENSE_INSURANCE_NUMBER TEXT NOT NULL
                        );
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE $TABLE_PURCHASES")
                database.execSQL(
                    """
                        CREATE TABLE $TABLE_PURCHASES (
                            $KEY_ID TEXT NOT NULL PRIMARY KEY,
                            $KEY_PURCHASE_BRAND TEXT NOT NULL,
                            $KEY_PURCHASE_STORE TEXT NOT NULL,
                            $KEY_PURCHASE_BORE1 TEXT NOT NULL,
                            $KEY_PURCHASE_UNITS INTEGER NOT NULL,
                            $KEY_PURCHASE_PRICE REAL NOT NULL,
                            $KEY_PURCHASE_DATE TEXT NOT NULL,
                            $KEY_PURCHASE_RATING REAL NOT NULL,
                            $KEY_PURCHASE_WEIGHT INTEGER NOT NULL,
                            $KEY_PURCHASE_IMAGE TEXT NOT NULL
                        );
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE $TABLE_PROPERTIES")
                database.execSQL(
                    """
                        CREATE TABLE $TABLE_PROPERTIES (
                            $KEY_ID TEXT NOT NULL PRIMARY KEY,
                            $KEY_PROPERTY_NICKNAME TEXT NOT NULL,
                            $KEY_PROPERTY_BRAND TEXT NOT NULL,
                            $KEY_PROPERTY_MODEL TEXT NOT NULL,
                            $KEY_PROPERTY_BORE1 TEXT NOT NULL,
                            $KEY_PROPERTY_BORE2 TEXT NOT NULL,
                            $KEY_PROPERTY_NUM_ID TEXT NOT NULL,
                            $KEY_PROPERTY_IMAGE TEXT NOT NULL
                        );
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE $TABLE_COMPETITION")
                database.execSQL(
                    """
                        CREATE TABLE $TABLE_COMPETITION (
                            $KEY_ID TEXT NOT NULL PRIMARY KEY,
                            $KEY_COMPETITION_DESCRIPTION TEXT NOT NULL,
                            $KEY_COMPETITION_DATE TEXT NOT NULL,
                            $KEY_COMPETITION_RANKING TEXT NOT NULL,
                            $KEY_COMPETITION_POINTS INTEGER NOT NULL,
                            $KEY_COMPETITION_PLACE TEXT NOT NULL
                        );
                    """.trimIndent()
                )

                database.setTransactionSuccessful()
                database.endTransaction()
            }
        }
    }
}
