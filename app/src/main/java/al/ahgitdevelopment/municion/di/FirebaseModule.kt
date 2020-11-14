package al.ahgitdevelopment.municion.di

import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSource
import al.ahgitdevelopment.municion.repository.firebase.RemoteStorageDataSourceContract
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class)
class FirebaseModule {

    @Provides
    fun provideCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    fun provideAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun providesDatabase(): FirebaseDatabase = Firebase.database

    @Provides
    fun provideStorage(): FirebaseStorage = Firebase.storage

    @Provides
    fun provideConnectivityManager(@ApplicationContext appContext: Context): ConnectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideTutorialImagesRepository(@ApplicationContext appContext: Context): RemoteStorageDataSourceContract =
        RemoteStorageDataSource(
            appContext,
            provideAuth(),
            provideStorage(),
            provideCrashlytics(),
            provideConnectivityManager(appContext)
        )
}
