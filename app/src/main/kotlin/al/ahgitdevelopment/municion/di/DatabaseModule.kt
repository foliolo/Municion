package al.ahgitdevelopment.municion.di

import android.content.Context
import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.dao.AppPurchaseDao
import al.ahgitdevelopment.municion.data.local.room.dao.CompraDao
import al.ahgitdevelopment.municion.data.local.room.dao.GuiaDao
import al.ahgitdevelopment.municion.data.local.room.dao.LicenciaDao
import al.ahgitdevelopment.municion.data.local.room.dao.TiradaDao
import al.ahgitdevelopment.municion.data.local.room.entities.AppPurchase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module para Room Database
 *
 * FASE 3: Dependency Injection
 * - Provee instancia singleton del database
 * - Provee DAOs para inyección en repositories
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMunicionDatabase(
        @ApplicationContext context: Context
    ): MunicionDatabase {
        return MunicionDatabase.create(context)
    }

    @Provides
    fun provideGuiaDao(database: MunicionDatabase): GuiaDao {
        return database.guiaDao()
    }

    @Provides
    fun provideCompraDao(database: MunicionDatabase): CompraDao {
        return database.compraDao()
    }

    @Provides
    fun provideLicenciaDao(database: MunicionDatabase): LicenciaDao {
        return database.licenciaDao()
    }

    @Provides
    fun provideTiradaDao(database: MunicionDatabase): TiradaDao {
        return database.tiradaDao()
    }

    @Provides
    fun provideAppPurchaseDao(database: MunicionDatabase): AppPurchaseDao {
        return database.appPurchaseDao()
    }
}
