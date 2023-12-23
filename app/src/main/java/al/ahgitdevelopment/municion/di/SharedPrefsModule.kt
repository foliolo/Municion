package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.preferences.SharedPreferencesManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SharedPrefsModule {

    @Provides
    fun provideSharedPrefs(@ApplicationContext appContext: Context): SharedPreferencesManager {
        return SharedPreferencesManager(appContext)
    }
}
