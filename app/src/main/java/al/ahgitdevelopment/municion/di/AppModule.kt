package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.utils.SimpleCountingIdlingResource
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun providesIdleResource(): SimpleCountingIdlingResource = SimpleCountingIdlingResource("Global")

    @Provides
    fun providesContext(@ApplicationContext appContext: Context): Context = appContext
}
