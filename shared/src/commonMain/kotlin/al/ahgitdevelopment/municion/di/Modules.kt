package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.analytics.AnalyticsTracker
import al.ahgitdevelopment.municion.analytics.FirebaseAnalyticsTracker
import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.buildMunicionDatabase
import al.ahgitdevelopment.municion.firebase.CrashReporter
import al.ahgitdevelopment.municion.firebase.CurrentUserIdProvider
import al.ahgitdevelopment.municion.firebase.FirebaseCurrentUserIdProvider
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Shared data graph: Firebase (GitLive), Room database + DAOs, repositories and the sync
 * subsystem. Populated incrementally across the migration phases.
 */
val dataModule = module {
    // Room database + DAOs (the RoomDatabase.Builder is provided by platformModule).
    single { buildMunicionDatabase(get()) }
    single { get<MunicionDatabase>().licenciaDao() }
    single { get<MunicionDatabase>().guiaDao() }
    single { get<MunicionDatabase>().compraDao() }
    single { get<MunicionDatabase>().tiradaDao() }
    single { get<MunicionDatabase>().appPurchaseDao() }
    single { get<MunicionDatabase>().syncOperationDao() }

    // Firebase common layer (GitLive)
    singleOf(::CrashReporter)
    singleOf(::FirebaseAnalyticsTracker) bind AnalyticsTracker::class
    singleOf(::FirebaseCurrentUserIdProvider) bind CurrentUserIdProvider::class

    // RTDB datasource + repositories + sync wired in phase 3.
}

/**
 * Presentation graph: ViewModels. Added per feature in phases 4–8.
 */
val presentationModule = module {
    // viewModelOf(::FeatureViewModel) added per feature.
}
