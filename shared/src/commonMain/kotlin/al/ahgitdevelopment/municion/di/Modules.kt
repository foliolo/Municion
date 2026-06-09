package al.ahgitdevelopment.municion.di

import org.koin.dsl.module

/**
 * Shared data graph: Firebase (GitLive), Room database + DAOs, repositories and the sync
 * subsystem. Populated incrementally across the migration phases.
 */
val dataModule = module {
    // Firebase / Room / repositories / sync wired in phases 1–3.
}

/**
 * Presentation graph: ViewModels. Added per feature in phases 4–8.
 */
val presentationModule = module {
    // viewModelOf(::FeatureViewModel) added per feature.
}
