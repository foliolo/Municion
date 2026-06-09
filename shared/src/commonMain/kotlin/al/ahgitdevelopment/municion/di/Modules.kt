package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.data.local.room.MunicionDatabase
import al.ahgitdevelopment.municion.data.local.room.buildMunicionDatabase
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

    // Firebase / repositories / sync wired in phases 1–3.
}

/**
 * Presentation graph: ViewModels. Added per feature in phases 4–8.
 */
val presentationModule = module {
    // viewModelOf(::FeatureViewModel) added per feature.
}
