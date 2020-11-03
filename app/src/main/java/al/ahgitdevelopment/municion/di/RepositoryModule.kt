package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.database.Repository
import al.ahgitdevelopment.municion.repository.database.dao.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent

@Module
@InstallIn(ApplicationComponent::class)
class RepositoryModule {

    @Provides
    fun providesRepository(database: AppDatabase): Repository =
        Repository(database)
}
