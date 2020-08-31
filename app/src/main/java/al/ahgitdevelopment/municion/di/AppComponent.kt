package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.ui.licenses.LicenseFormFragment
import al.ahgitdevelopment.municion.ui.licenses.LicensesFragment
import al.ahgitdevelopment.municion.ui.login.LoginPasswordFragment
import al.ahgitdevelopment.municion.ui.properties.PropertyFragment
import al.ahgitdevelopment.municion.ui.purchases.PurchasesFragment
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
    fun inject(propertyFragment: PropertyFragment)
    fun inject(purchasesFragment: PurchasesFragment)
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