package al.ahgitdevelopment.municion

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class con Hilt
 *
 * FASE 3: Hilt Dependency Injection
 * - @HiltAndroidApp habilita DI en toda la app
 * - Punto de entrada para todos los módulos Hilt
 *
 * IMPORTANTE: Actualizar AndroidManifest.xml:
 * <application android:name=".MunicionApplication" ...>
 *
 * @since v3.0.0 (TRACK B Modernization)
 */
@HiltAndroidApp
class MunicionApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializaciones globales si son necesarias
        android.util.Log.i("MunicionApplication", "App initialized with Hilt DI")
    }
}
