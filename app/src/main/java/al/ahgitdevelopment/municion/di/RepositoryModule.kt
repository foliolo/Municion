package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.DataSourceContract
import al.ahgitdevelopment.municion.repository.DefaultRepository
import al.ahgitdevelopment.municion.repository.RepositoryContract
import al.ahgitdevelopment.municion.repository.database.AppDatabase
import al.ahgitdevelopment.municion.repository.database.LocalDataSource
import al.ahgitdevelopment.municion.repository.firebase.RemoteDataSource
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @TypeLocalDataSource
    @Provides
    fun providesLocalDataSource(database: AppDatabase): DataSourceContract {
        return LocalDataSource(database)
    }

    @TypeRemoteDataSource
    @Provides
    fun providesRemoteDataSource(firebase: FirebaseDatabase, auth: FirebaseAuth): DataSourceContract {
        return RemoteDataSource(firebase, auth)
    }
}

/**
 * The binding for Repository is on its own module so that we can replace it easily in tests.
 */
@Module
@InstallIn(SingletonComponent::class)
object GenericRepositoryModule {

    @Provides
    fun providesRepository(
        @ApplicationContext appContext: Context,
        @TypeLocalDataSource localDataSource: DataSourceContract,
        @TypeRemoteDataSource remoteDataSource: DataSourceContract
    ): RepositoryContract {
        return DefaultRepository(appContext, localDataSource, remoteDataSource)
    }
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeRemoteDataSource

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeLocalDataSource
