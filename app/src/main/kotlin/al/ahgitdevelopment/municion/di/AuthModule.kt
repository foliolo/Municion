package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.auth.AuthManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module para autenticación local
 *
 * FASE 1: Auth Modernization
 * - Provee AuthManager como singleton
 * - Gestiona PIN encriptado y biometría
 *
 * @since v3.0.0 (TRACK B - Auth Modernization)
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthManager(
        @ApplicationContext context: Context
    ): AuthManager {
        return AuthManager(context)
    }
}
