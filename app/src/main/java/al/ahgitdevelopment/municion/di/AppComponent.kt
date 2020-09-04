package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.NavigationActivity
import al.ahgitdevelopment.municion.ui.competitions.CompetitionFragment
import al.ahgitdevelopment.municion.ui.licenses.LicenseFormFragment
import al.ahgitdevelopment.municion.ui.licenses.LicensesFragment
import al.ahgitdevelopment.municion.ui.login.LoginPasswordFragment
import al.ahgitdevelopment.municion.ui.properties.PropertiesFragment
import al.ahgitdevelopment.municion.ui.properties.PropertyFormFragment
import al.ahgitdevelopment.municion.ui.purchases.PurchaseFormFragment
import al.ahgitdevelopment.municion.ui.purchases.PurchasesFragment
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
    fun inject(propertiesFragment: PropertiesFragment)
    fun inject(purchasesFragment: PurchasesFragment)
    fun inject(licensesFragment: LicensesFragment)
    fun inject(competitionFragment: CompetitionFragment)

    fun inject(propertyFormFragment: PropertyFormFragment)
    fun inject(licenseFormFragment: LicenseFormFragment)
    fun inject(purchaseFormFragment: PurchaseFormFragment)

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