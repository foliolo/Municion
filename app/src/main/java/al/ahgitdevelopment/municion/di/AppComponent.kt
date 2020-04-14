package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.login.LoginPasswordFragment
import android.content.Context
import dagger.Component

@Component(
        modules = [SharedPrefsModule::class, ViewModelModule::class]
)
interface AppComponent {

    fun inject(loginPasswordFragment: LoginPasswordFragment)

    companion object {
        fun create(context: Context): AppComponent {
            return DaggerAppComponent.builder()
                    .sharedPrefsModule(SharedPrefsModule(context))
                    .build()
        }
    }
}