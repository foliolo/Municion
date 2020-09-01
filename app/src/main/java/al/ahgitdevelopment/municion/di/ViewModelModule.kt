package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.ui.competitions.CompetitionViewModel
import al.ahgitdevelopment.municion.ui.licenses.LicenseFormViewModel
import al.ahgitdevelopment.municion.ui.licenses.LicensesViewModel
import al.ahgitdevelopment.municion.ui.login.LoginViewModel
import al.ahgitdevelopment.municion.ui.properties.PropertiesViewModel
import al.ahgitdevelopment.municion.ui.properties.PropertyFormViewModel
import al.ahgitdevelopment.municion.ui.purchases.PurchasesViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLogingViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PropertiesViewModel::class)
    abstract fun bindPropertyViewModel(propertiesViewModel: PropertiesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PurchasesViewModel::class)
    abstract fun bindPurchaseViewModel(purchasesViewModel: PurchasesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LicensesViewModel::class)
    abstract fun bindLicensesViewModel(licensesViewModel: LicensesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CompetitionViewModel::class)
    abstract fun bindCompetitionsViewModel(competitionViewModel: CompetitionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LicenseFormViewModel::class)
    abstract fun bindLicenseFormViewModel(licenseFormViewModel: LicenseFormViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PropertyFormViewModel::class)
    abstract fun bindPropertyFormViewModel(propertyFormViewModel: PropertyFormViewModel): ViewModel

    @Binds
    abstract fun bindsModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}

@Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

class ViewModelFactory @Inject constructor(
        private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>) :
        ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
                ?: creators.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
                ?: throw IllegalArgumentException("unknown model class $modelClass")

        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }
}
