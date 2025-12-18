package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.data.repository.ImageRepository
import al.ahgitdevelopment.municion.data.repository.ImageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module para Repository bindings
 *
 * @since v3.2.2 (Image Upload Feature)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        imageRepositoryImpl: ImageRepositoryImpl
    ): ImageRepository
}
