package al.ahgitdevelopment.municion.di

import dagger.Component

@Component(
    modules = [
        SharedPrefsModule::class,
        FirebaseModule::class,
        DatabaseModule::class
    ]
)
interface AppComponent
