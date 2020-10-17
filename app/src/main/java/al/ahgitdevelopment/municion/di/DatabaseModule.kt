package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import al.ahgitdevelopment.municion.repository.dao.DATABASE_NAME
import al.ahgitdevelopment.municion.repository.dao.LicenseDao
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides

@Module
class DatabaseModule(val context: Context) {

    private val database = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
        // .addMigrations(MIGRATION_1_2)
        .build()

    @Provides
    fun providesRoomDatabase(): AppDatabase = database

    @Provides
    fun providesLicenseDao(): LicenseDao = database.licenseDao()!!

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
