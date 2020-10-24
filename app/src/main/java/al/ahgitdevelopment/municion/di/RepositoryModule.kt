package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.Repository
import al.ahgitdevelopment.municion.repository.RepositoryInterface
import al.ahgitdevelopment.municion.repository.dao.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
class RepositoryModule {

    @Provides
    fun providesRepository(database: AppDatabase): RepositoryInterface =
        Repository(database)
}
