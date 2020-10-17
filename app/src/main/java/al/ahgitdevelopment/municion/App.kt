package al.ahgitdevelopment.municion

import al.ahgitdevelopment.municion.di.AppComponent
import android.app.Application

open class App : Application() {

    val appComponent: AppComponent by lazy {
        AppComponent.create(applicationContext)
    }
}
