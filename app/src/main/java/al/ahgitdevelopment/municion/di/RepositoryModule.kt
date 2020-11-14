package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.DataSourceContract
import al.ahgitdevelopment.municion.repository.DefaultRepository
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.database.AppDatabase
import al.ahgitdevelopment.municion.repository.database.LocalDataSource
import al.ahgitdevelopment.municion.repository.firebase.RemoteDataSource
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Qualifier

@Module
@InstallIn(ApplicationComponent::class)
class RepositoryModule {

    @TypeLocalDataSource
    @Provides
    fun providesLocalDataSource(
        @ApplicationContext appContext: Context,
        database: AppDatabase
    ): DataSourceContract {
        return LocalDataSource(database)
    }

    @TypeRemoteDataSource
    @Provides
    fun providesRemoteDataSource(firebase: FirebaseDatabase): DataSourceContract {
        return RemoteDataSource(firebase)
    }
}

/**
 * The binding for Repository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(ApplicationComponent::class)
object GenericRepositoryModule {

    @Provides
    fun providesRepository(
        @TypeLocalDataSource localDataSource: DataSourceContract,
        @TypeRemoteDataSource remoteDataSource: DataSourceContract
    ): RepositoryContract {
        return DefaultRepository(localDataSource, remoteDataSource)
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeRemoteDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeLocalDataSource
