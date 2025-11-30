package al.ahgitdevelopment.municion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class con Hilt
 *
 * FASE 3: Hilt Dependency Injection
 * - @HiltAndroidApp habilita DI en toda la app
 * - Punto de entrada para todos los módulos Hilt
 * - Extiende BaseApplication para mantener compatibilidad con código legacy
 *   (FragmentMainActivity.java usa BaseApplication.crashlytics)
 *
 * IMPORTANTE: Actualizar AndroidManifest.xml:
 * <application android:name=".MunicionApplication" ...>
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltAndroidApp
class MunicionApplication : Application()
