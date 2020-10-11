package al.ahgitdevelopment.municion.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides

@Module
class SharedPrefsModule(private val context: Context) {

    @Provides
    fun provideSharedPrefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    companion object {
        const val PREFS_PASSWORD = "password"
        const val PREFS_SHOW_ADS = "show_ads"
        const val PREFS_SHOW_TUTORIAL = "tutorial"
    }
}
