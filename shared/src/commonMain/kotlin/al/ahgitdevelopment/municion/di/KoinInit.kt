package al.ahgitdevelopment.municion.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

/**
 * Starts Koin with the platform, data and presentation modules.
 *
 * Idempotent: iOS calls this from the Compose entry point, which may run more than once
 * across view-controller recreations, so a second call is a no-op instead of crashing.
 */
fun initKoin(config: KoinAppDeclaration? = null) {
    val alreadyStarted = runCatching { KoinPlatform.getKoin() }.isSuccess
    if (alreadyStarted) return

    startKoin {
        config?.invoke(this)
        modules(platformModule, dataModule, presentationModule)
    }
}
