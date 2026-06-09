package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.ads.NoOpRemoveAdsManager
import al.ahgitdevelopment.municion.ads.RemoveAdsManager
import al.ahgitdevelopment.municion.analytics.AnalyticsTracker
import al.ahgitdevelopment.municion.analytics.FirebaseAnalyticsTracker
import al.ahgitdevelopment.municion.auth.AuthViewModel
import al.ahgitdevelopment.municion.auth.FirebaseAuthRepository
import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.buildMunicionDatabase
import al.ahgitdevelopment.municion.data.repository.CompraRepository
import al.ahgitdevelopment.municion.data.repository.FirebaseImageStorageRepository
import al.ahgitdevelopment.municion.data.repository.GuiaRepository
import al.ahgitdevelopment.municion.data.repository.ImageStorageRepository
import al.ahgitdevelopment.municion.data.repository.LicenciaRepository
import al.ahgitdevelopment.municion.data.repository.TiradaRepository
import al.ahgitdevelopment.municion.data.sync.MunicionRtdbDatasource
import al.ahgitdevelopment.municion.data.sync.SyncOutboxDrainer
import al.ahgitdevelopment.municion.data.sync.SyncOutboxEnqueuer
import al.ahgitdevelopment.municion.domain.usecase.ClearLocalDataUseCase
import al.ahgitdevelopment.municion.domain.usecase.CreateCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.DeleteCompraUseCase
import al.ahgitdevelopment.municion.domain.usecase.SyncDataUseCase
import al.ahgitdevelopment.municion.domain.usecase.UpdateCompraUseCase
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import al.ahgitdevelopment.municion.firebase.FirebaseCurrentUserIdProvider
import al.ahgitdevelopment.municion.ui.forms.compra.CompraFormViewModel
import al.ahgitdevelopment.municion.ui.forms.guia.GuiaFormViewModel
import al.ahgitdevelopment.municion.ui.forms.licencia.LicenciaFormViewModel
import al.ahgitdevelopment.municion.ui.forms.tirada.TiradaFormViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.AccountSettingsViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.CompraViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.GuiaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.LicenciaViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.LoginViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MainViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.MigrationViewModel
import al.ahgitdevelopment.municion.ui.viewmodel.TiradaViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Shared data graph: Firebase (GitLive), Room database + DAOs, repositories and the sync
 * subsystem. Populated incrementally across the migration phases.
 */
val dataModule =
    module {
        // Room database + DAOs (the RoomDatabase.Builder is provided by platformModule).
        single { buildMunicionDatabase(get()) }
        single { get<MunicionDatabase>().licenciaDao() }
        single { get<MunicionDatabase>().guiaDao() }
        single { get<MunicionDatabase>().compraDao() }
        single { get<MunicionDatabase>().tiradaDao() }
        single { get<MunicionDatabase>().appPurchaseDao() }
        single { get<MunicionDatabase>().syncOperationDao() }

        // Firebase common layer (GitLive)
        single<FirebaseDatabase> { Firebase.database }
        single<FirebaseAuth> { Firebase.auth }
        singleOf(::FirebaseAuthRepository)
        singleOf(::CrashReporter)
        singleOf(::FirebaseAnalyticsTracker) bind AnalyticsTracker::class
        // Remove-ads entitlement (RevenueCat impl wired in phase 7 once SDK keys are configured).
        single<RemoveAdsManager> { NoOpRemoveAdsManager() }
        singleOf(::FirebaseCurrentUserIdProvider) bind CurrentUserIdProvider::class

        // Sync subsystem (SyncScheduler is provided by platformModule)
        singleOf(::MunicionRtdbDatasource)
        singleOf(::SyncOutboxEnqueuer)
        singleOf(::SyncOutboxDrainer)

        // Repositories
        singleOf(::FirebaseImageStorageRepository) bind ImageStorageRepository::class
        singleOf(::LicenciaRepository)
        singleOf(::GuiaRepository)
        singleOf(::CompraRepository)
        singleOf(::TiradaRepository)

        // Use cases
        singleOf(::CreateCompraUseCase)
        singleOf(::UpdateCompraUseCase)
        singleOf(::DeleteCompraUseCase)
        singleOf(::ClearLocalDataUseCase)
        singleOf(::SyncDataUseCase)
    }

/**
 * Presentation graph: ViewModels. Added per feature in phases 4–8.
 */
val presentationModule =
    module {
        viewModelOf(::AuthViewModel)
        viewModelOf(::MainViewModel)
        viewModelOf(::LicenciaViewModel)
        viewModelOf(::GuiaViewModel)
        viewModelOf(::CompraViewModel)
        viewModelOf(::TiradaViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::MigrationViewModel)
        viewModelOf(::LicenciaFormViewModel)
        viewModelOf(::GuiaFormViewModel)
        viewModelOf(::CompraFormViewModel)
        viewModelOf(::TiradaFormViewModel)
        viewModelOf(::AccountSettingsViewModel)
    }
