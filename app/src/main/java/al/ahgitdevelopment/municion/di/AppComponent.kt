package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.ui.compras.ComprasFragment
import al.ahgitdevelopment.municion.ui.guias.GuiasFragment
import al.ahgitdevelopment.municion.ui.licencias.LicenseFormFragment
import al.ahgitdevelopment.municion.ui.licencias.LicensesFragment
import al.ahgitdevelopment.municion.ui.login.LoginPasswordFragment
import al.ahgitdevelopment.municion.ui.tiradas.TiradasFragment
import android.content.Context
import dagger.Component

@Component(
    modules = [
        SharedPrefsModule::class,
        ViewModelModule::class,
        FirebaseModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent {

    fun inject(navigationActivity: NavigationActivity)

    fun inject(loginPasswordFragment: LoginPasswordFragment)
    fun inject(guiasFragment: GuiasFragment)
    fun inject(comprasFragment: ComprasFragment)
    fun inject(licensesFragment: LicensesFragment)
    fun inject(tiradasFragment: TiradasFragment)

    fun inject(licenciasFormFragment: LicenseFormFragment)

    companion object {
        fun create(context: Context): AppComponent {
            return DaggerAppComponent.builder()
                .firebaseModule(FirebaseModule())
                .sharedPrefsModule(SharedPrefsModule(context))
                .databaseModule(DatabaseModule(context))
                .build()
        }
    }
}