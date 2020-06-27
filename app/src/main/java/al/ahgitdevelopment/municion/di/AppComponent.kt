package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.FragmentMainContent
import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.compras.ComprasFragment
import al.ahgitdevelopment.municion.licencias.LicenciasFragment
import al.ahgitdevelopment.municion.login.LoginPasswordFragment
import android.content.Context
import dagger.Component

@Component(
    modules = [
        SharedPrefsModule::class,
        ViewModelModule::class,
        FirebaseModule::class
    ]
)
interface AppComponent {

    fun inject(navigationActivity: NavigationActivity)

    fun inject(loginPasswordFragment: LoginPasswordFragment)
    fun inject(licenciasFragment: LicenciasFragment)
    fun inject(comprasFragment: ComprasFragment)

    companion object {
        fun create(context: Context): AppComponent {
            return DaggerAppComponent.builder()
                .firebaseModule(FirebaseModule())
                .sharedPrefsModule(SharedPrefsModule(context))
                .build()
        }
    }
}