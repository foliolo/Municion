package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.ui.compras.ComprasViewModel
import al.ahgitdevelopment.municion.ui.guias.GuiasViewModel
import al.ahgitdevelopment.municion.ui.licencias.LicenciasViewModel
import al.ahgitdevelopment.municion.ui.login.LoginViewModel
import al.ahgitdevelopment.municion.ui.tiradas.TiradasViewModel
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
    @ViewModelKey(GuiasViewModel::class)
    abstract fun bindGuiasViewModel(guiasViewModel: GuiasViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ComprasViewModel::class)
    abstract fun bindComprasViewModel(comprasViewModel: ComprasViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LicenciasViewModel::class)
    abstract fun bindLicenciasViewModel(licenciasViewModel: LicenciasViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TiradasViewModel::class)
    abstract fun bindTiradasViewModel(tiradasViewModel: TiradasViewModel): ViewModel

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
